package com.iwan.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.iwan.blog.entity.Notification;
import org.apache.ibatis.annotations.Mapper;

/**
 * 通知数据访问层接口
 * 
 * <p>该接口继承MyBatis-Plus的BaseMapper，提供基础的CRUD操作。
 * 
 * <p>主要功能：
 * <ul>
 *   <li>查询用户的通知列表</li>
 *   <li>标记通知为已读</li>
 *   <li>统计未读通知数量</li>
 * </ul>
 */
@Mapper
public interface NotificationMapper extends BaseMapper<Notification> {
    // MyBatis-Plus会自动提供基础的CRUD方法
    // 如需自定义SQL，可在此添加方法并使用@Select、@Update等注解
}