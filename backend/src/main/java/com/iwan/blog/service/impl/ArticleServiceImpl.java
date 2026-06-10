package com.iwan.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iwan.blog.dto.ArticleDTO;
import com.iwan.blog.entity.Article;
import com.iwan.blog.mapper.ArticleMapper;
import com.iwan.blog.service.ArticleService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class ArticleServiceImpl implements ArticleService {

    private final ArticleMapper articleMapper;
    private final ObjectMapper objectMapper;

    public ArticleServiceImpl(ArticleMapper articleMapper, ObjectMapper objectMapper) {
        this.articleMapper = articleMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public Article create(ArticleDTO dto, Long userId) {
        Article article = new Article();
        Map<String, Object> doc = new HashMap<>();
        doc.put("title", dto.getTitle());
        doc.put("content", dto.getContent());
        doc.put("categoryId", dto.getCategoryId());
        doc.put("tagList", dto.getTagList());
        doc.put("status", dto.getStatus());
        doc.put("cover", dto.getCover());
        doc.put("authorId", userId);
        doc.put("readCount", 0);
        doc.put("likeCount", 0);
        doc.put("collectCount", 0);
        doc.put("commentCount", 0);
        doc.put("authorName", "");

        try {
            article.setDoc(objectMapper.writeValueAsString(doc));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化失败", e);
        }

        articleMapper.insert(article);
        return article;
    }

    @Override
    @Transactional
    public Article update(Long id, ArticleDTO dto) {
        Article article = getById(id);
        if (article == null) {
            throw new RuntimeException("文章不存在");
        }

        try {
            Map<String, Object> doc = objectMapper.readValue(article.getDoc(), Map.class);
            doc.put("title", dto.getTitle());
            doc.put("content", dto.getContent());
            doc.put("categoryId", dto.getCategoryId());
            doc.put("tagList", dto.getTagList());
            doc.put("status", dto.getStatus());
            doc.put("cover", dto.getCover());
            article.setDoc(objectMapper.writeValueAsString(doc));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化失败", e);
        }

        articleMapper.updateById(article);
        return article;
    }

    @Override
    public Article getById(Long id) {
        return articleMapper.selectById(id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        articleMapper.deleteById(id);
    }

    @Override
    public IPage<Article> list(Page<Article> page, String keyword, String categoryId, String tagId, Integer status, String sortBy) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getIsDeleted, false);
        
        // 筛选条件
        if (status != null) {
            wrapper.apply("doc->>'status' = {0}", status.toString());
        }
        if (categoryId != null && !categoryId.isEmpty()) {
            wrapper.apply("doc->>'categoryId' = {0}", categoryId);
        }
        if (tagId != null && !tagId.isEmpty()) {
            wrapper.apply("doc->'tagList' ? {0}", tagId);
        }
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.apply("doc->>'title' LIKE {0}", "%" + keyword + "%");
        }
        
        // 排序
        if ("hot".equals(sortBy)) {
            // 热度排序：综合阅读、点赞、收藏、评论
            wrapper.apply("ORDER BY (COALESCE((doc->>'readCount')::int, 0) + COALESCE((doc->>'likeCount')::int, 0) * 2 + COALESCE((doc->>'collectCount')::int, 0) * 3 + COALESCE((doc->>'commentCount')::int, 0) * 4) DESC");
        } else {
            // 默认按时间排序
            wrapper.orderByDesc(Article::getCreateTime);
        }
        
        return articleMapper.selectPage(page, wrapper);
    }

    @Override
    public IPage<Article> getUserArticles(Page<Article> page, Long userId) {
        LambdaQueryWrapper<Article> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Article::getIsDeleted, false)
               .apply("doc->>'authorId' = {0}", userId.toString())
               .orderByDesc(Article::getCreateTime);
        return articleMapper.selectPage(page, wrapper);
    }
}
