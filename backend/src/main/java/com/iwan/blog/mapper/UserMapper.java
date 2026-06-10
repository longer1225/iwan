package com.iwan.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iwan.blog.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT * FROM sys_user WHERE doc->>'username' = #{username} AND is_deleted = false")
    User findByUsername(String username);
}
