package com.iwan.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iwan.blog.dto.ArticleDTO;
import com.iwan.blog.entity.Article;

public interface ArticleService {

    Article create(ArticleDTO dto, Long userId);

    Article update(Long id, ArticleDTO dto);

    Article getById(Long id);

    void delete(Long id);

    IPage<Article> list(Page<Article> page, String keyword, String categoryId, String tagId, Integer status, String sortBy);

    IPage<Article> getUserArticles(Page<Article> page, Long userId);

    void updateReadCount(Long articleId);
}
