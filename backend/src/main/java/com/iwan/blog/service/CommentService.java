package com.iwan.blog.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iwan.blog.dto.CommentDTO;
import com.iwan.blog.entity.Comment;

import java.util.List;

public interface CommentService {

    Comment create(CommentDTO dto, Long userId);

    Comment getById(Long id);

    void delete(Long id);

    IPage<Comment> list(Page<Comment> page, Long articleId);
    
    /**
     * 点赞评论
     */
    Comment like(Long commentId, Long userId);
    
    /**
     * 检查是否已点赞
     */
    boolean isLiked(Long commentId, Long userId);
    
    /**
     * 获取评论的回复列表
     */
    List<Comment> getReplies(Long parentId);
}
