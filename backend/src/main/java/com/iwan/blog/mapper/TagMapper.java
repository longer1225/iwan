package com.iwan.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iwan.blog.entity.Tag;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {
}