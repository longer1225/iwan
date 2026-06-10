package com.iwan.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iwan.blog.entity.Comment;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CommentMapper extends BaseMapper<Comment> {
}
