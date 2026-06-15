// 业务服务实现类所在包
package com.iwan.blog.service.impl;

// 导入数据库实体类：行为记录表、文章表、用户表
import com.iwan.blog.entity.Action;
import com.iwan.blog.entity.Article;
import com.iwan.blog.entity.User;
// 导入对应的Mapper接口（MyBatis-Plus数据访问层）
import com.iwan.blog.mapper.ActionMapper;
import com.iwan.blog.mapper.ArticleMapper;
import com.iwan.blog.mapper.UserMapper;
// 导入业务接口
import com.iwan.blog.service.ActionService;
// 分页返回视图对象，封装分页数据给前端
import com.iwan.blog.vo.PageVO;
// MyBatis-Plus 条件构造器：Lambda查询、Lambda更新
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
// MyBatis-Plus 分页核心对象
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
// Jackson JSON序列化/反序列化相关
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
// 日志框架
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// Spring服务注解
import org.springframework.stereotype.Service;
// 事务注解
import org.springframework.transaction.annotation.Transactional;

// 集合类
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 行为记录 服务实现类
 * 功能：实现 点赞、取消点赞、收藏、取消收藏、查询我的点赞/收藏、判断是否已点赞/收藏
 * 数据库说明：
 * 1. Action 表：记录用户行为（点赞/收藏），doc 字段为 PostgreSQL jsonb 类型
 * 2. Article 表：文章表，doc 字段为 jsonb，内部存储 likeCount、collectCount 等统计字段
 * 3. 依赖自定义 JsonbTypeHandler 实现 jsonb <--> Java String 转换
 */
@Service // 标记为Spring业务服务Bean，交由容器管理
public class ActionServiceImpl implements ActionService {

    // 声明日志对象，用于打印运行日志、排查问题
    private static final Logger logger = LoggerFactory.getLogger(ActionServiceImpl.class);

    // 注入数据访问层
    private final ActionMapper actionMapper;     // 行为记录Mapper
    private final ObjectMapper objectMapper;     // Jackson JSON工具类
    private final ArticleMapper articleMapper;   // 文章Mapper
    private final UserMapper userMapper;         // 用户Mapper

    /**
     * 构造器注入（Spring推荐注入方式）
     * @param actionMapper 行为记录Mapper
     * @param objectMapper JSON工具
     * @param articleMapper 文章Mapper
     * @param userMapper 用户Mapper
     */
    public ActionServiceImpl(ActionMapper actionMapper, ObjectMapper objectMapper,
                            ArticleMapper articleMapper, UserMapper userMapper) {
        this.actionMapper = actionMapper;
        this.objectMapper = objectMapper;
        this.articleMapper = articleMapper;
        this.userMapper = userMapper;
    }

    /**
     * 点赞/取消点赞 切换逻辑（点击一次点赞，再点击一次取消）
     * @param userId 当前用户ID
     * @param targetId 被点赞目标ID（文章ID）
     * @param targetType 目标类型（文章/评论等）
     * @Transactional 开启事务：整个方法要么全部成功，要么全部回滚
     */
    @Override
    @Transactional
    public void toggleLike(Long userId, String targetId, String targetType) {
        // 打印入参日志，方便调试
        logger.info("===== toggleLike called - userId: {}, targetId: {}, targetType: {}", userId, targetId, targetType);

        // 1. 构建查询条件
        LambdaQueryWrapper<Action> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Action::getIsDeleted, false)                // 条件1：未逻辑删除
                // 条件2：查询 jsonb 字段 doc 内的 userId 值 = 当前用户ID（PostgreSQL jsonb语法）
                .apply("doc->>'userId' = {0}", userId.toString())
                // 条件3：doc内 targetId = 目标ID
                .apply("doc->>'targetId' = {0}", targetId)
                // 条件4：行为类型为 点赞 LIKE
                .apply("doc->>'actionType' = 'LIKE'");

        // 根据条件查询单条记录：判断用户是否已经点过赞
        Action existing = actionMapper.selectOne(wrapper);
        logger.info("===== toggleLike existing record found: {}", existing != null);

        // 分支1：已存在点赞记录 --> 执行【取消点赞】
        if (existing != null) {
            logger.info("===== toggleLike deleting existing record, id: {}", existing.getId());
            // 构建更新条件：根据主键ID更新
            LambdaUpdateWrapper<Action> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Action::getId, existing.getId())
                    .set(Action::getIsDeleted, true); // 逻辑删除（软删除）

            // 执行更新，标记为已删除
            int result = actionMapper.update(null, updateWrapper);
            logger.info("===== toggleLike update result: {}", result);

            // 文章点赞数 -1
            updateArticleLikeCount(Long.parseLong(targetId), -1);
        }
        // 分支2：无点赞记录 --> 执行【新增点赞】
        else {
            logger.info("===== toggleLike creating new record");
            Action action = new Action(); // 新建行为记录实体

            // 组装存入jsonb字段的JSON数据
            Map<String, Object> doc = new HashMap<>();
            doc.put("userId", userId.toString());
            doc.put("targetId", targetId);
            doc.put("targetType", targetType);
            doc.put("actionType", "LIKE"); // 行为类型：点赞

            try {
                // Map 转 JSON 字符串，存入Action的doc字段（由JsonbTypeHandler转为数据库jsonb）
                action.setDoc(objectMapper.writeValueAsString(doc));
                // 插入数据库
                actionMapper.insert(action);
                logger.info("===== toggleLike created new record, id: {}", action.getId());

                // 文章点赞数 +1
                updateArticleLikeCount(Long.parseLong(targetId), 1);
            } catch (JsonProcessingException e) {
                // JSON转换异常，抛出运行时异常，事务自动回滚
                throw new RuntimeException("创建点赞记录失败", e);
            }
        }
    }

    /**
     * 收藏/取消收藏 切换逻辑
     * @param userId 当前用户ID
     * @param targetId 目标文章ID
     * @param targetType 目标类型
     */
    @Override
    @Transactional
    public void toggleCollect(Long userId, String targetId, String targetType) {
        logger.info("===== toggleCollect called - userId: {}, targetId: {}, targetType: {}", userId, targetId, targetType);

        // 构建查询条件：查询当前用户对该文章的收藏记录
        LambdaQueryWrapper<Action> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Action::getIsDeleted, false)
                .apply("doc->>'userId' = {0}", userId.toString())
                .apply("doc->>'targetId' = {0}", targetId)
                .apply("doc->>'actionType' = 'COLLECT'"); // 行为类型：收藏

        Action existing = actionMapper.selectOne(wrapper);
        logger.info("===== toggleCollect existing record found: {}", existing != null);

        // 已收藏 --> 取消收藏（逻辑删除）
        if (existing != null) {
            logger.info("===== toggleCollect deleting existing record, id: {}", existing.getId());
            LambdaUpdateWrapper<Action> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Action::getId, existing.getId())
                    .set(Action::getIsDeleted, true);

            int result = actionMapper.update(null, updateWrapper);
            logger.info("===== toggleCollect update result: {}", result);

            // 文章收藏数 -1
            updateArticleCollectCount(Long.parseLong(targetId), -1);
        }
        // 未收藏 --> 新增收藏记录
        else {
            logger.info("===== toggleCollect creating new record");
            Action action = new Action();
            Map<String, Object> doc = new HashMap<>();
            doc.put("userId", userId.toString());
            doc.put("targetId", targetId);
            doc.put("targetType", targetType);
            doc.put("actionType", "COLLECT");

            try {
                action.setDoc(objectMapper.writeValueAsString(doc));
                actionMapper.insert(action);
                logger.info("===== toggleCollect created new record, id: {}", action.getId());

                // 文章收藏数 +1
                updateArticleCollectCount(Long.parseLong(targetId), 1);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("创建收藏记录失败", e);
            }
        }
    }

    /**
     * 私有方法：更新文章点赞数量
     * @param articleId 文章ID
     * @param delta 变化值：+1 点赞 / -1 取消点赞
     */
    private void updateArticleLikeCount(Long articleId, int delta) {
        // 根据ID查询文章
        Article article = articleMapper.selectById(articleId);
        // 文章存在 且 doc字段不为空
        if (article != null && article.getDoc() != null) {
            try {
                // JSON字符串 反序列化为 Map
                Map<String, Object> doc = objectMapper.readValue(article.getDoc(), new TypeReference<Map<String, Object>>() {});
                // 获取当前点赞数，不存在则默认0
                int currentCount = doc.containsKey("likeCount") ? ((Number) doc.get("likeCount")).intValue() : 0;
                // 数量变更，最小为0（防止负数）
                currentCount = Math.max(0, currentCount + delta);
                // 写入新的点赞数
                doc.put("likeCount", currentCount);
                // Map 转回 JSON 字符串，重新设置给实体
                article.setDoc(objectMapper.writeValueAsString(doc));
                // 更新文章数据到数据库
                articleMapper.updateById(article);
                logger.info("===== Updated likeCount for article {}: {}", articleId, currentCount);
            } catch (JsonProcessingException e) {
                logger.error("Failed to update likeCount for article {}", articleId, e);
            }
        }
    }

    /**
     * 私有方法：更新文章收藏数量
     * @param articleId 文章ID
     * @param delta 变化值：+1 收藏 / -1 取消收藏
     */
    private void updateArticleCollectCount(Long articleId, int delta) {
        Article article = articleMapper.selectById(articleId);
        if (article != null && article.getDoc() != null) {
            try {
                Map<String, Object> doc = objectMapper.readValue(article.getDoc(), new TypeReference<Map<String, Object>>() {});
                int currentCount = doc.containsKey("collectCount") ? ((Number) doc.get("collectCount")).intValue() : 0;
                currentCount = Math.max(0, currentCount + delta);
                doc.put("collectCount", currentCount);
                article.setDoc(objectMapper.writeValueAsString(doc));
                articleMapper.updateById(article);
                logger.info("===== Updated collectCount for article {}: {}", articleId, currentCount);
            } catch (JsonProcessingException e) {
                logger.error("Failed to update collectCount for article {}", articleId, e);
            }
        }
    }

    /**
     * 分页查询：获取当前用户点赞的所有文章
     * @param userId 用户ID
     * @param pageNum 页码
     * @param pageSize 每页条数
     * @return 分页视图对象，封装文章信息+作者信息
     */
    @Override
    public PageVO<Map<String, Object>> getUserLikes(Long userId, Integer pageNum, Integer pageSize) {
        logger.info("===== getUserLikes called with userId: {}", userId);

        // 1. 创建分页对象（当前页、每页条数）
        Page<Action> page = new Page<>(pageNum, pageSize);
        // 2. 构建查询条件：查询当前用户未删除的点赞记录
        LambdaQueryWrapper<Action> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Action::getIsDeleted, false)
                .apply("doc->>'userId' = {0}", userId.toString())
                .apply("doc->>'actionType' = 'LIKE'")
                .orderByDesc(Action::getCreateTime); // 按创建时间倒序（最新点赞在前）

        // 3. 分页查询行为记录
        IPage<Action> result = actionMapper.selectPage(page, wrapper);
        logger.info("===== getUserLikes query result size: {}", result.getRecords().size());
        logger.info("===== getUserLikes query total: {}", result.getTotal());

        // 遍历分页结果，组装 文章+作者 信息
        for (Action action : result.getRecords()) {
            logger.info("===== Action record doc: {}", action.getDoc());
        }

        // Stream流处理每条点赞记录，关联查询文章、作者
        List<Map<String, Object>> records = result.getRecords().stream()
                .map(action -> {
                    try {
                        // 行为记录的jsonb字符串转Map
                        Map<String, Object> actionData = objectMapper.readValue(action.getDoc(), new TypeReference<Map<String, Object>>() {});
                        String targetId = (String) actionData.get("targetId");
                        logger.info("===== Processing action with targetId: {}", targetId);

                        // 根据文章ID查询文章详情
                        Article article = articleMapper.selectById(Long.parseLong(targetId));
                        logger.info("===== Article found for targetId {}: {}", targetId, article != null);

                        if (article != null) {
                            // 文章doc字段转Map
                            Map<String, Object> articleData = objectMapper.readValue(article.getDoc(), new TypeReference<Map<String, Object>>() {});
                            // ID转为字符串，避免前端JS长数字精度丢失
                            articleData.put("id", String.valueOf(article.getId()));
                            // 补充文章创建、更新时间
                            articleData.put("createTime", article.getCreateTime());
                            articleData.put("updateTime", article.getUpdateTime());

                            // 获取文章作者ID
                            Object authorIdObj = articleData.get("authorId");
                            logger.info("===== Article authorId: {}", authorIdObj);

                            if (authorIdObj != null) {
                                Long authorId = null;
                                // 兼容 authorId 为Long / String两种类型
                                if (authorIdObj instanceof Long) {
                                    authorId = (Long) authorIdObj;
                                } else if (authorIdObj instanceof String) {
                                    authorId = Long.parseLong((String) authorIdObj);
                                }

                                if (authorId != null) {
                                    // 根据作者ID查询用户信息
                                    User author = userMapper.selectById(authorId);
                                    logger.info("===== Author found for authorId {}: {}", authorId, author != null);
                                    if (author != null && author.getDoc() != null) {
                                        // 解析用户jsonb字段，获取昵称、头像
                                        Map<String, Object> authorData = objectMapper.readValue(author.getDoc(), new TypeReference<Map<String, Object>>() {});
                                        articleData.put("authorName", authorData.get("nickname"));
                                        articleData.put("authorAvatar", authorData.get("avatar"));
                                    } else {
                                        // 作者不存在，填充默认值
                                        articleData.put("authorName", "未知作者");
                                        articleData.put("authorAvatar", "");
                                    }
                                }
                            } else {
                                articleData.put("authorName", "未知作者");
                                articleData.put("authorAvatar", "");
                            }
                            return articleData;
                        } else {
                            logger.warn("===== Article not found for targetId: {}", targetId);
                        }
                    } catch (Exception e) {
                        logger.error("Error processing action", e);
                    }
                    // 文章不存在/异常，返回空Map
                    return new HashMap<String, Object>();
                })
                .filter(map -> !map.isEmpty()) // 过滤掉空数据
                .toList();

        logger.info("===== Final records size after processing: {}", records.size());
        // 组装分页VO返回给前端
        return PageVO.of((long) records.size(), result.getPages(), result.getCurrent(), result.getSize(), records);
    }

    /**
     * 分页查询：获取当前用户收藏的所有文章
     * 逻辑和 getUserLikes 完全一致，仅查询行为类型为 COLLECT
     */
    @Override
    public PageVO<Map<String, Object>> getUserCollects(Long userId, Integer pageNum, Integer pageSize) {
        logger.info("===== getUserCollects called with userId: {}", userId);

        Page<Action> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Action> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Action::getIsDeleted, false)
                .apply("doc->>'userId' = {0}", userId.toString())
                .apply("doc->>'actionType' = 'COLLECT'")
                .orderByDesc(Action::getCreateTime);

        IPage<Action> result = actionMapper.selectPage(page, wrapper);
        logger.info("===== getUserCollects query result size: {}", result.getRecords().size());
        logger.info("===== getUserCollects query total: {}", result.getTotal());

        List<Map<String, Object>> records = result.getRecords().stream()
                .map(action -> {
                    try {
                        Map<String, Object> actionData = objectMapper.readValue(action.getDoc(), new TypeReference<Map<String, Object>>() {});
                        String targetId = (String) actionData.get("targetId");
                        logger.info("===== Processing collect action with targetId: {}", targetId);

                        Article article = articleMapper.selectById(Long.parseLong(targetId));
                        logger.info("===== Article found for targetId {}: {}", targetId, article != null);

                        if (article != null) {
                            Map<String, Object> articleData = objectMapper.readValue(article.getDoc(), new TypeReference<Map<String, Object>>() {});
                            articleData.put("id", String.valueOf(article.getId()));
                            articleData.put("createTime", article.getCreateTime());
                            articleData.put("updateTime", article.getUpdateTime());

                            Object authorIdObj = articleData.get("authorId");
                            logger.info("===== Article authorId: {}", authorIdObj);

                            if (authorIdObj != null) {
                                Long authorId = null;
                                if (authorIdObj instanceof Long) {
                                    authorId = (Long) authorIdObj;
                                } else if (authorIdObj instanceof String) {
                                    authorId = Long.parseLong((String) authorIdObj);
                                }

                                if (authorId != null) {
                                    User author = userMapper.selectById(authorId);
                                    logger.info("===== Author found for authorId {}: {}", authorId, author != null);
                                    if (author != null && author.getDoc() != null) {
                                        Map<String, Object> authorData = objectMapper.readValue(author.getDoc(), new TypeReference<Map<String, Object>>() {});
                                        articleData.put("authorName", authorData.get("nickname"));
                                        articleData.put("authorAvatar", authorData.get("avatar"));
                                    } else {
                                        articleData.put("authorName", "未知作者");
                                        articleData.put("authorAvatar", "");
                                    }
                                }
                            } else {
                                articleData.put("authorName", "未知作者");
                                articleData.put("authorAvatar", "");
                            }
                            return articleData;
                        } else {
                            logger.warn("===== Article not found for targetId: {}", targetId);
                        }
                    } catch (Exception e) {
                        logger.error("Error processing collect action", e);
                    }
                    return new HashMap<String, Object>();
                })
                .filter(map -> !map.isEmpty())
                .toList();

        logger.info("===== Final collect records size after processing: {}", records.size());
        return PageVO.of((long) records.size(), result.getPages(), result.getCurrent(), result.getSize(), records);
    }

    /**
     * 判断用户是否已点赞该文章
     * @param userId 用户ID
     * @param targetId 文章ID
     * @param targetType 目标类型
     * @return true=已点赞  false=未点赞
     */
    @Override
    public boolean isLiked(Long userId, String targetId, String targetType) {
        logger.info("===== isLiked called - userId: {}, targetId: {}, targetType: {}, targetId type: {}",
                userId, targetId, targetType, targetId != null ? targetId.getClass().getName() : "null");

        LambdaQueryWrapper<Action> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Action::getIsDeleted, false)
                .apply("doc->>'userId' = {0}", userId.toString())
                .apply("doc->>'targetId' = {0}", targetId)
                .apply("doc->>'actionType' = 'LIKE'");

        // exists：判断是否存在符合条件的数据
        boolean exists = actionMapper.exists(wrapper);
        logger.info("===== isLiked result: {}", exists);
        return exists;
    }

    /**
     * 判断用户是否已收藏该文章
     * @param userId 用户ID
     * @param targetId 文章ID
     * @param targetType 目标类型
     * @return true=已收藏  false=未收藏
     */
    @Override
    public boolean isCollected(Long userId, String targetId, String targetType) {
        LambdaQueryWrapper<Action> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Action::getIsDeleted, false)
                .apply("doc->>'userId' = {0}", userId.toString())
                .apply("doc->>'targetId' = {0}", targetId)
                .apply("doc->>'actionType' = 'COLLECT'");

        return actionMapper.exists(wrapper);
    }
}