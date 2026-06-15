package com.iwan.blog.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 通知实体类
 * 
 * <p>该类对应数据库中的notification表，用于存储用户的各种通知消息。
 * 
 * <p>支持的通知类型：
 * <ul>
 *   <li>FRIEND_REQUEST - 好友请求</li>
 *   <li>COMMENT - 评论通知</li>
 *   <li>LIKE - 点赞通知</li>
 *   <li>SYSTEM - 系统通知</li>
 * </ul>
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("notification")
public class Notification extends BaseEntity {

    /**
     * 接收通知的用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 通知类型
     */
    @TableField("type")
    private String type;

    /**
     * 通知标题
     */
    @TableField("title")
    private String title;

    /**
     * 通知内容
     */
    @TableField("content")
    private String content;

    /**
     * 发送通知的用户ID（可为null，如系统通知）
     */
    @TableField("sender_id")
    private Long senderId;

    /**
     * 发送用户昵称
     */
    @TableField("sender_name")
    private String senderName;

    /**
     * 发送用户头像
     */
    @TableField("sender_avatar")
    private String senderAvatar;

    /**
     * 关联的实体ID（如文章ID、评论ID等）
     */
    @TableField("related_id")
    private Long relatedId;

    /**
     * 跳转链接（可选）
     */
    @TableField("link")
    private String link;

    /**
     * 额外数据（JSON格式，用于存储扩展信息）
     */
    @TableField("extra_data")
    private String extraData;

    /**
     * 是否已读
     */
    @TableField("is_read")
    private Boolean isRead;
}