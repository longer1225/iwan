package com.iwan.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iwan.blog.dto.CommentDTO;
import com.iwan.blog.entity.Comment;

public interface CommentService {

    Comment create(CommentDTO dto, Long userId);

    Comment getById(Long id);

    void delete(Long id);

    IPage<Comment> list(Page<Comment> page, Long articleId);
}
