
package com.iwan.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iwan.blog.dto.ArticleDTO;
import com.iwan.blog.entity.Article;
import com.iwan.blog.mapper.ArticleMapper;
import com.iwan.blog.service.ArticleService;
import com.iwan.blog.service.PermissionService;
import com.iwan.blog.util.JsonbUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * 文章服务实现类
 * 
 * <p>该类实现了ArticleService接口，提供文章的CRUD操作。
 * 通过依赖注入JsonbUtils工具类，解耦了JSONB字段的操作逻辑。
 * 
 * <p>核心功能：
 * <ul>
 *   <li>文章创建：使用JsonbUtils构建JSONB文档</li>
 *   <li>文章更新：使用JsonbUtils解析和更新JSONB文档</li>
 *   <li>文章查询：支持分页、关键词搜索、分类筛选、标签筛选</li>
 *   <li>阅读计数：使用JsonbUtils安全更新阅读量</li>
 * </ul>
 */
@Service
public class ArticleServiceImpl implements ArticleService {

    /**
     * 文章数据访问层
     */
    private final ArticleMapper articleMapper;
    
    /**
     * JSONB操作工具类（解耦JSON处理逻辑）
     */
    private final JsonbUtils jsonbUtils;
    
    /**
     * 权限服务（用于文章可见性过滤）
     */
    private final PermissionService permissionService;

    /**
     * 构造函数依赖注入
     * 
     * @param articleMapper 文章Mapper
     * @param jsonbUtils JSONB工具类
     * @param permissionService 权限服务
     */
    public ArticleServiceImpl(ArticleMapper articleMapper, JsonbUtils jsonbUtils, 
                             PermissionService permissionService) {
        this.articleMapper = articleMapper;
        this.jsonbUtils = jsonbUtils;
        this.permissionService = permissionService;
    }

    /**
     * 创建文章
     * 
     * <p>使用JsonbUtils.buildArticleDoc()构建文章的JSONB文档，
     * 避免在Service层直接处理JSON序列化逻辑。
     * 
     * @param dto 文章数据传输对象
     * @param userId 作者ID
     * @return 创建成功的文章实体
     */
    @Override
    @Transactional
    public Article create(ArticleDTO dto, Long userId) {
        Article article = new Article();
        
        // 使用JsonbUtils构建文章文档，解耦文档构建逻辑
        Map<String, Object> doc = jsonbUtils.buildArticleDoc(
                dto.getTitle(),
                dto.getContent(),
                dto.getCategoryId(),
                dto.getTagList(),
                dto.getStatus(),
                dto.getCover(),
                userId,
                dto.getAnonymous(),
                dto.getVisibility() != null ? dto.getVisibility() : "PUBLIC",
                dto.getGroupId()
        );
        
        // 使用JsonbUtils将Map转换为JSON字符串
        article.setDoc(jsonbUtils.toJson(doc));
        
        // 插入数据库
        articleMapper.insert(article);
        return article;
    }

    /**
     * 更新文章
     * 
     * <p>使用JsonbUtils.parseJson()解析现有文档，更新后再序列化回JSON。
     * 
     * @param id 文章ID
     * @param dto 文章数据传输对象
     * @return 更新后的文章实体
     * @throws RuntimeException 文章不存在时抛出异常
     */
    @Override
    @Transactional
    public Article update(Long id, ArticleDTO dto) {
        // 查询文章
        Article article = getById(id);
        if (article == null) {
            throw new RuntimeException("文章不存在");
        }

        // 使用JsonbUtils解析JSONB文档
        Map<String, Object> doc = jsonbUtils.parseJson(article.getDoc());
        
        // 更新文档字段
        doc.put("title", dto.getTitle());
        doc.put("content", dto.getContent());
        doc.put("categoryId", dto.getCategoryId());
        doc.put("tagList", dto.getTagList());
        doc.put("status", dto.getStatus());
        doc.put("cover", dto.getCover());
        
        // 更新权限设置（仅当传入非空值时）
        if (dto.getVisibility() != null) {
            doc.put("visibility", dto.getVisibility());
        }
        doc.put("groupId", dto.getGroupId());
        
        // 使用JsonbUtils序列化回JSON
        article.setDoc(jsonbUtils.toJson(doc));

        // 更新数据库
        articleMapper.updateById(article);
        return article;
    }

    /**
     * 根据ID获取文章
     * 
     * @param id 文章ID
     * @return 文章实体
     */
    @Override
    public Article getById(Long id) {
        return articleMapper.selectById(id);
    }

    /**
     * 删除文章
     * 
     * @param id 文章ID
     */
    @Override
    @Transactional
    public void delete(Long id) {
        articleMapper.deleteById(id);
    }

    /**
     * 分页查询文章列表
     * 
     * <p>支持多种筛选条件：关键词、分类、标签、状态，
     * 并通过PermissionService进行可见性过滤。
     * 
     * @param page 分页对象
     * @param keyword 关键词（搜索标题）
     * @param categoryId 分类ID
     * @param tagId 标签ID
     * @param status 状态
     * @param sortBy 排序方式（hot/默认时间排序）
     * @param userId 当前用户ID（用于权限判断）
     * @return 分页结果
     */
    @Override
    public IPage<Article> list(Page<Article> page, String keyword, String categoryId, 
                               String tagId, Integer status, String sortBy, Long userId) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        
        // 基础条件：未删除
        wrapper.eq(Article::getIsDeleted, false);
        
        // 权限过滤：根据用户ID判断可见性
        wrapper.apply(permissionService.getVisibilityCondition(userId));
        
        // 状态筛选
        if (status != null) {
            wrapper.apply("doc->>'status' = {0}", status.toString());
        }
        
        // 分类筛选
        if (categoryId != null && !categoryId.isEmpty()) {
            wrapper.apply("doc->>'categoryId' = {0}", categoryId);
        }
        
        // 标签筛选（使用JSONB数组包含查询）
        if (tagId != null && !tagId.isEmpty()) {
            wrapper.apply("doc->'tagList' ? {0}", tagId);
        }
        
        // 关键词搜索（标题模糊匹配）
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.apply("doc->>'title' LIKE {0}", "%" + keyword + "%");
        }
        
        // 排序处理
        if ("hot".equals(sortBy)) {
            // 热度排序：综合阅读、点赞、收藏、评论（加权计算）
            wrapper.apply("ORDER BY (COALESCE((doc->>'readCount')::int, 0) + " +
                    "COALESCE((doc->>'likeCount')::int, 0) * 2 + " +
                    "COALESCE((doc->>'collectCount')::int, 0) * 3 + " +
                    "COALESCE((doc->>'commentCount')::int, 0) * 4) DESC");
        } else {
            // 默认按创建时间降序
            wrapper.orderByDesc(Article::getCreateTime);
        }
        
        return articleMapper.selectPage(page, wrapper);
    }

    /**
     * 获取用户发布的文章列表
     * 
     * @param page 分页对象
     * @param userId 用户ID
     * @return 用户文章分页结果
     */
    @Override
    public IPage<Article> getUserArticles(Page<Article> page, Long userId) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getIsDeleted, false)
               .apply("doc->>'authorId' = {0}", userId.toString())
               .orderByDesc(Article::getCreateTime);
        return articleMapper.selectPage(page, wrapper);
    }

    /**
     * 更新文章阅读计数
     * 
     * <p>使用JsonbUtils安全地解析和更新JSONB字段中的阅读计数。
     * 
     * @param articleId 文章ID
     */
    @Override
    @Transactional
    public void updateReadCount(Long articleId) {
        Article article = articleMapper.selectById(articleId);
        if (article != null && article.getDoc() != null) {
            // 使用JsonbUtils解析文档
            Map<String, Object> doc = jsonbUtils.parseJson(article.getDoc());
            
            // 安全获取当前阅读计数（处理null和类型转换）
            Integer currentCount = jsonbUtils.getIntValue(doc, "readCount");
            if (currentCount == null) {
                currentCount = 0;
            }
            
            // 更新阅读计数
            doc.put("readCount", currentCount + 1);
            
            // 使用JsonbUtils序列化回JSON
            article.setDoc(jsonbUtils.toJson(doc));
            
            // 更新数据库
            articleMapper.updateById(article);
        }
    }
}
