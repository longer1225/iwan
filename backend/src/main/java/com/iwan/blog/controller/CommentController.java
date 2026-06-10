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
import java.util.Map;

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
}
