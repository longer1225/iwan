package com.iwan.blog.service;

import com.iwan.blog.entity.Article;

public interface PermissionService {

    /**
     * 检查用户是否有权限查看文章
     * @param article 文章实体
     * @param userId 当前用户ID（可为null，表示匿名访客）
     * @return 是否有权限
     */
    boolean canViewArticle(Article article, Long userId);

    /**
     * 获取文章可见权限SQL条件
     * @param userId 当前用户ID（可为null，表示匿名访客）
     * @return SQL条件字符串
     */
    String getVisibilityCondition(Long userId);
}