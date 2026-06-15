package com.iwan.blog.service.impl;

import com.iwan.blog.entity.Article;
import com.iwan.blog.service.PermissionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PermissionServiceImpl implements PermissionService {

    private static final Logger logger = LoggerFactory.getLogger(PermissionServiceImpl.class);

    private final ObjectMapper objectMapper;

    public PermissionServiceImpl(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean canViewArticle(Article article, Long userId) {
        if (article == null || article.getDoc() == null) {
            return false;
        }

        try {
            Map<String, Object> doc = objectMapper.readValue(article.getDoc(), new TypeReference<Map<String, Object>>() {});
            String visibility = (String) doc.get("visibility");
            Long authorId = null;
            if (doc.containsKey("authorId")) {
                Object authorIdObj = doc.get("authorId");
                if (authorIdObj instanceof Number) {
                    authorId = ((Number) authorIdObj).longValue();
                } else if (authorIdObj instanceof String) {
                    authorId = Long.parseLong((String) authorIdObj);
                }
            }

            // 匿名访客只能查看公开内容（包括visibility为null的情况，视为公开）
            if (userId == null) {
                return "PUBLIC".equals(visibility) || visibility == null;
            }

            // 作者可以查看自己的所有文章
            if (authorId != null && authorId.equals(userId)) {
                return true;
            }

            // 根据权限类型判断
            switch (visibility) {
                case "PUBLIC":
                    return true;
                case "FRIENDS_ONLY":
                    // 后续需要检查好友关系
                    return true; // 暂时允许所有登录用户查看
                case "PRIVATE":
                    return authorId != null && authorId.equals(userId);
                case "GROUP":
                    // 后续需要检查分组关系
                    return true; // 暂时允许所有登录用户查看
                default:
                    return "PUBLIC".equals(visibility);
            }

        } catch (JsonProcessingException e) {
            logger.error("Failed to parse article doc", e);
            return false;
        }
    }

    @Override
    public String getVisibilityCondition(Long userId) {
        if (userId == null) {
            // 匿名访客：只能查看公开内容
            return "(doc->>'visibility' = 'PUBLIC' OR doc->>'visibility' IS NULL)";
        }

        // 登录用户：可以查看公开内容 + 自己的所有内容
        return String.format(
            "(doc->>'visibility' = 'PUBLIC' OR doc->>'visibility' IS NULL OR (doc->>'authorId')::bigint = %d OR doc->>'visibility' = 'FRIENDS_ONLY' OR doc->>'visibility' = 'GROUP')",
            userId
        );
    }
}