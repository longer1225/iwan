package com.iwan.blog.service.impl;

import com.iwan.blog.entity.Action;
import com.iwan.blog.entity.Article;
import com.iwan.blog.entity.User;
import com.iwan.blog.mapper.ActionMapper;
import com.iwan.blog.mapper.ArticleMapper;
import com.iwan.blog.mapper.UserMapper;
import com.iwan.blog.service.ActionService;
import com.iwan.blog.vo.PageVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ActionServiceImpl implements ActionService {

    private static final Logger logger = LoggerFactory.getLogger(ActionServiceImpl.class);

    private final ActionMapper actionMapper;
    private final ObjectMapper objectMapper;
    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;

    public ActionServiceImpl(ActionMapper actionMapper, ObjectMapper objectMapper, 
                            ArticleMapper articleMapper, UserMapper userMapper) {
        this.actionMapper = actionMapper;
        this.objectMapper = objectMapper;
        this.articleMapper = articleMapper;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public void toggleLike(Long userId, String targetId, String targetType) {
        LambdaQueryWrapper<Action> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Action::getIsDeleted, false)
                .apply("doc->>'userId' = {0}", userId.toString())
                .apply("doc->>'targetId' = {0}", targetId)
                .apply("doc->>'actionType' = 'LIKE'");

        Action existing = actionMapper.selectOne(wrapper);

        if (existing != null) {
            existing.setIsDeleted(true);
            actionMapper.updateById(existing);
        } else {
            Action action = new Action();
            Map<String, Object> doc = new HashMap<>();
            doc.put("userId", userId.toString());
            doc.put("targetId", targetId);
            doc.put("targetType", targetType);
            doc.put("actionType", "LIKE");
            try {
                action.setDoc(objectMapper.writeValueAsString(doc));
                actionMapper.insert(action);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("创建点赞记录失败", e);
            }
        }
    }

    @Override
    @Transactional
    public void toggleCollect(Long userId, String targetId, String targetType) {
        LambdaQueryWrapper<Action> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Action::getIsDeleted, false)
                .apply("doc->>'userId' = {0}", userId.toString())
                .apply("doc->>'targetId' = {0}", targetId)
                .apply("doc->>'actionType' = 'COLLECT'");

        Action existing = actionMapper.selectOne(wrapper);

        if (existing != null) {
            existing.setIsDeleted(true);
            actionMapper.updateById(existing);
        } else {
            Action action = new Action();
            Map<String, Object> doc = new HashMap<>();
            doc.put("userId", userId.toString());
            doc.put("targetId", targetId);
            doc.put("targetType", targetType);
            doc.put("actionType", "COLLECT");
            try {
                action.setDoc(objectMapper.writeValueAsString(doc));
                actionMapper.insert(action);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("创建收藏记录失败", e);
            }
        }
    }

    @Override
    public PageVO<Map<String, Object>> getUserLikes(Long userId, Integer pageNum, Integer pageSize) {
        logger.info("===== getUserLikes called with userId: {}", userId);
        
        Page<Action> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Action> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Action::getIsDeleted, false)
                .apply("doc->>'userId' = {0}", userId.toString())
                .apply("doc->>'actionType' = 'LIKE'")
                .orderByDesc(Action::getCreateTime);

        IPage<Action> result = actionMapper.selectPage(page, wrapper);
        logger.info("===== getUserLikes query result size: {}", result.getRecords().size());
        logger.info("===== getUserLikes query total: {}", result.getTotal());
        
        for (Action action : result.getRecords()) {
            logger.info("===== Action record doc: {}", action.getDoc());
        }

        List<Map<String, Object>> records = result.getRecords().stream()
                .map(action -> {
                    try {
                        Map<String, Object> actionData = objectMapper.readValue(action.getDoc(), new TypeReference<Map<String, Object>>() {});
                        String targetId = (String) actionData.get("targetId");
                        logger.info("===== Processing action with targetId: {}", targetId);
                        
                        // 获取文章信息
                        Article article = articleMapper.selectById(Long.parseLong(targetId));
                        logger.info("===== Article found for targetId {}: {}", targetId, article != null);
                        
                        if (article != null) {
                            Map<String, Object> articleData = objectMapper.readValue(article.getDoc(), new TypeReference<Map<String, Object>>() {});
                            // 使用字符串类型返回ID，避免JavaScript数字精度问题
                            articleData.put("id", String.valueOf(article.getId()));
                            articleData.put("createTime", article.getCreateTime());
                            articleData.put("updateTime", article.getUpdateTime());
                            
                            // 获取作者信息 - 使用authorId字段
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
                        logger.error("Error processing action", e);
                    }
                    return new HashMap<String, Object>();
                })
                .filter(map -> !map.isEmpty())
                .toList();

        logger.info("===== Final records size after processing: {}", records.size());
        // 使用实际存在的文章数量作为total
        return PageVO.of((long) records.size(), result.getPages(), result.getCurrent(), result.getSize(), records);
    }

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
                        
                        // 获取文章信息
                        Article article = articleMapper.selectById(Long.parseLong(targetId));
                        logger.info("===== Article found for targetId {}: {}", targetId, article != null);
                        
                        if (article != null) {
                            Map<String, Object> articleData = objectMapper.readValue(article.getDoc(), new TypeReference<Map<String, Object>>() {});
                            // 使用字符串类型返回ID，避免JavaScript数字精度问题
                            articleData.put("id", String.valueOf(article.getId()));
                            articleData.put("createTime", article.getCreateTime());
                            articleData.put("updateTime", article.getUpdateTime());
                            
                            // 获取作者信息 - 使用authorId字段
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
        // 使用实际存在的文章数量作为total
        return PageVO.of((long) records.size(), result.getPages(), result.getCurrent(), result.getSize(), records);
    }

    @Override
    public boolean isLiked(Long userId, String targetId, String targetType) {
        LambdaQueryWrapper<Action> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Action::getIsDeleted, false)
                .apply("doc->>'userId' = {0}", userId.toString())
                .apply("doc->>'targetId' = {0}", targetId)
                .apply("doc->>'actionType' = 'LIKE'");

        return actionMapper.exists(wrapper);
    }

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
