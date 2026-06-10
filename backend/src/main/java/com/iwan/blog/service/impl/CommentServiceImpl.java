package com.iwan.blog.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iwan.blog.dto.CommentDTO;
import com.iwan.blog.entity.Comment;
import com.iwan.blog.mapper.CommentMapper;
import com.iwan.blog.service.CommentService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class CommentServiceImpl implements CommentService {

    private final CommentMapper commentMapper;
    private final ObjectMapper objectMapper;

    public CommentServiceImpl(CommentMapper commentMapper, ObjectMapper objectMapper) {
        this.commentMapper = commentMapper;
        this.objectMapper = objectMapper;
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
        doc.put("anonymous", dto.getAnonymous());
        doc.put("likeCount", 0);

        try {
            comment.setDoc(objectMapper.writeValueAsString(doc));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化失败", e);
        }

        commentMapper.insert(comment);
        return comment;
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
        return commentMapper.selectPage(page, null);
    }
}
