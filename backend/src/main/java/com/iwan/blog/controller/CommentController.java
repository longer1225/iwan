package com.iwan.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iwan.blog.dto.CommentDTO;
import com.iwan.blog.entity.Comment;
import com.iwan.blog.service.CommentService;
import com.iwan.blog.vo.PageVO;
import com.iwan.blog.vo.ResponseVO;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping
    public ResponseVO<PageVO<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam String articleId) {

        Page<Comment> page = new Page<>(pageNum, pageSize);
        IPage<Comment> result = commentService.list(page, Long.parseLong(articleId));

        java.util.List<Map<String, Object>> records = new java.util.ArrayList<>();
        for (Comment comment : result.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", comment.getId().toString());
            item.put("content", comment.getDoc() != null ? extractFromDoc(comment.getDoc(), "content") : "");
            item.put("userName", comment.getDoc() != null ? extractFromDoc(comment.getDoc(), "userName") : "匿名用户");
            item.put("userAvatar", comment.getDoc() != null ? extractFromDoc(comment.getDoc(), "userAvatar") : "");
            item.put("createTime", comment.getCreateTime());
            item.put("likeCount", comment.getDoc() != null ? getIntFromDoc(comment.getDoc(), "likeCount", 0) : 0);
            records.add(item);
        }

        return ResponseVO.success(PageVO.of(result.getTotal(), result.getPages(),
                result.getCurrent(), result.getSize(), records));
    }

    @PostMapping
    public ResponseVO<Map<String, Object>> create(@Valid @RequestBody CommentDTO dto) {
        String userId = null;
        try {
            userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        } catch (Exception e) {
            if (!Boolean.TRUE.equals(dto.getAnonymous())) {
                return ResponseVO.unauthorized();
            }
        }

        Comment comment = commentService.create(dto, userId != null ? Long.parseLong(userId) : null);

        Map<String, Object> result = new HashMap<>();
        result.put("id", comment.getId());
        result.put("content", dto.getContent());

        return ResponseVO.success(result);
    }

    @DeleteMapping("/{id}")
    public ResponseVO<Void> delete(@PathVariable Long id) {
        commentService.delete(id);
        return ResponseVO.success();
    }

    /**
     * 点赞评论
     */
    @PostMapping("/{id}/like")
    public ResponseVO<Map<String, Object>> like(@PathVariable Long id) {
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Comment comment = commentService.like(id, Long.parseLong(userId));

        Map<String, Object> result = new HashMap<>();
        result.put("id", comment.getId());
        result.put("likeCount", comment.getDoc() != null ? getIntFromDoc(comment.getDoc(), "likeCount", 0) : 0);

        return ResponseVO.success(result);
    }

    /**
     * 检查是否已点赞
     */
    @GetMapping("/{id}/liked")
    public ResponseVO<Map<String, Object>> isLiked(@PathVariable Long id) {
        String userId = null;
        try {
            userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("liked", false);
            return ResponseVO.success(result);
        }

        boolean liked = commentService.isLiked(id, Long.parseLong(userId));
        Map<String, Object> result = new HashMap<>();
        result.put("liked", liked);

        return ResponseVO.success(result);
    }

    /**
     * 获取评论的回复列表
     */
    @GetMapping("/{id}/replies")
    public ResponseVO<List<Map<String, Object>>> getReplies(@PathVariable Long id) {
        List<Comment> replies = commentService.getReplies(id);

        List<Map<String, Object>> result = replies.stream().map(comment -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", comment.getId().toString());
            item.put("content", comment.getDoc() != null ? extractFromDoc(comment.getDoc(), "content") : "");
            item.put("userName", comment.getDoc() != null ? extractFromDoc(comment.getDoc(), "userName") : "匿名用户");
            item.put("userAvatar", comment.getDoc() != null ? extractFromDoc(comment.getDoc(), "userAvatar") : "");
            item.put("createTime", comment.getCreateTime());
            item.put("likeCount", comment.getDoc() != null ? getIntFromDoc(comment.getDoc(), "likeCount", 0) : 0);
            return item;
        }).collect(Collectors.toList());

        return ResponseVO.success(result);
    }

    private String extractFromDoc(String doc, String key) {
        if (doc == null || doc.isEmpty()) {
            return "";
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.Map<String, Object> map = mapper.readValue(doc, java.util.Map.class);
            Object value = map.get(key);
            return value != null ? value.toString() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private int getIntFromDoc(String doc, String key, int defaultValue) {
        if (doc == null || doc.isEmpty()) {
            return defaultValue;
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.Map<String, Object> map = mapper.readValue(doc, java.util.Map.class);
            Object value = map.get(key);
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
