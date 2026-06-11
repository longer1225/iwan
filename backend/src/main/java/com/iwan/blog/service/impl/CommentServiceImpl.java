package com.iwan.blog.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iwan.blog.dto.CommentDTO;
import com.iwan.blog.entity.Article;
import com.iwan.blog.entity.Comment;
import com.iwan.blog.mapper.ArticleMapper;
import com.iwan.blog.mapper.CommentMapper;
import com.iwan.blog.service.CommentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommentServiceImpl implements CommentService {

    private static final Logger logger = LoggerFactory.getLogger(CommentServiceImpl.class);

    private final CommentMapper commentMapper;
    private final ObjectMapper objectMapper;
    private final ArticleMapper articleMapper;

    public CommentServiceImpl(CommentMapper commentMapper, ObjectMapper objectMapper, ArticleMapper articleMapper) {
        this.commentMapper = commentMapper;
        this.objectMapper = objectMapper;
        this.articleMapper = articleMapper;
    }

    @Override
    @Transactional
    public Comment create(CommentDTO dto, Long userId) {
        Comment comment = new Comment();
        Map<String, Object> doc = new HashMap<>();
        doc.put("articleId", dto.getArticleId());
        doc.put("parentId", dto.getParentId());
        doc.put("content", dto.getContent());
        doc.put("userId", userId);
        doc.put("userName", dto.getUserName() != null ? dto.getUserName() : "匿名用户");
        doc.put("userAvatar", dto.getUserAvatar() != null ? dto.getUserAvatar() : "");
        doc.put("anonymous", dto.getAnonymous());
        doc.put("likeCount", 0);

        try {
            comment.setDoc(objectMapper.writeValueAsString(doc));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化失败", e);
        }

        commentMapper.insert(comment);
        
        // 更新文章的评论计数
        updateArticleCommentCount(Long.parseLong(dto.getArticleId()), 1);
        
        return comment;
    }
    
    /**
     * 更新文章评论数
     */
    private void updateArticleCommentCount(Long articleId, int delta) {
        Article article = articleMapper.selectById(articleId);
        if (article != null && article.getDoc() != null) {
            try {
                Map<String, Object> doc = objectMapper.readValue(article.getDoc(), new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {});
                int currentCount = doc.containsKey("commentCount") ? ((Number) doc.get("commentCount")).intValue() : 0;
                currentCount = Math.max(0, currentCount + delta);
                doc.put("commentCount", currentCount);
                article.setDoc(objectMapper.writeValueAsString(doc));
                articleMapper.updateById(article);
                logger.info("===== Updated commentCount for article {}: {}", articleId, currentCount);
            } catch (JsonProcessingException e) {
                logger.error("Failed to update commentCount for article {}", articleId, e);
            }
        }
    }

    @Override
    public Comment getById(Long id) {
        return commentMapper.selectById(id);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        commentMapper.deleteById(id);
    }

    @Override
    public IPage<Comment> list(Page<Comment> page, Long articleId) {
        return commentMapper.selectPage(page, 
            Wrappers.<Comment>lambdaQuery()
                .eq(Comment::getIsDeleted, false)
                .apply("doc->>'parentId' IS NULL OR doc->>'parentId' = ''")
                .apply("doc->>'articleId' = {0}", articleId.toString())
                .orderByDesc(Comment::getCreateTime));
    }
    
    @Override
    @Transactional
    public Comment like(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getIsDeleted()) {
            throw new RuntimeException("评论不存在");
        }
        
        try {
            Map<String, Object> doc = objectMapper.readValue(comment.getDoc(), new TypeReference<Map<String, Object>>() {});
            
            // 获取当前点赞列表
            List<String> likedUserIds = new ArrayList<>();
            if (doc.containsKey("likedUserIds") && doc.get("likedUserIds") instanceof List) {
                List<?> tempList = (List<?>) doc.get("likedUserIds");
                for (Object obj : tempList) {
                    likedUserIds.add(String.valueOf(obj));
                }
            }
            
            String userIdStr = userId.toString();
            int currentCount = doc.containsKey("likeCount") ? ((Number) doc.get("likeCount")).intValue() : 0;
            
            if (likedUserIds.contains(userIdStr)) {
                // 取消点赞
                likedUserIds.remove(userIdStr);
                currentCount = Math.max(0, currentCount - 1);
            } else {
                // 点赞
                likedUserIds.add(userIdStr);
                currentCount++;
            }
            
            doc.put("likedUserIds", likedUserIds);
            doc.put("likeCount", currentCount);
            comment.setDoc(objectMapper.writeValueAsString(doc));
            commentMapper.updateById(comment);
            
            logger.info("===== Comment {} like count updated to {}", commentId, currentCount);
            return comment;
            
        } catch (JsonProcessingException e) {
            logger.error("Failed to like comment {}", commentId, e);
            throw new RuntimeException("点赞失败", e);
        }
    }
    
    @Override
    public boolean isLiked(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getIsDeleted()) {
            return false;
        }
        
        try {
            Map<String, Object> doc = objectMapper.readValue(comment.getDoc(), new TypeReference<Map<String, Object>>() {});
            
            if (doc.containsKey("likedUserIds") && doc.get("likedUserIds") instanceof List) {
                List<?> likedUserIds = (List<?>) doc.get("likedUserIds");
                return likedUserIds.contains(userId.toString());
            }
        } catch (JsonProcessingException e) {
            logger.error("Failed to check if comment {} is liked", commentId, e);
        }
        
        return false;
    }
    
    @Override
    public List<Comment> getReplies(Long parentId) {
        List<Comment> comments = commentMapper.selectList(
            Wrappers.<Comment>lambdaQuery()
                .eq(Comment::getIsDeleted, false)
                .apply("doc->>'parentId' IS NOT NULL AND doc->>'parentId' != ''")
                .apply("CAST(doc->>'parentId' AS BIGINT) = {0}", parentId)
                .orderByAsc(Comment::getCreateTime)
        );
        return comments != null ? comments : new ArrayList<>();
    }
}
