package com.iwan.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 通知消息数据传输对象
 * 
 * <p>该类封装了各种通知消息的通用结构，包括：
 * <ul>
 *   <li>好友请求通知</li>
 *   <li>评论通知</li>
 *   <li>点赞通知</li>
 *   <li>系统通知</li>
 * </ul>
 * 
 * <p>实现Serializable接口以支持消息序列化传输。
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationMessage implements Serializable {

    /**
     * 通知类型
     * FRIEND_REQUEST - 好友请求
     * COMMENT - 评论
     * LIKE - 点赞
     * SYSTEM - 系统通知
     */
    private String type;

    /**
     * 接收通知的用户ID
     */
    private Long receiverId;

    /**
     * 发送通知的用户ID（可为null，如系统通知）
     */
    private Long senderId;

    /**
     * 发送用户昵称
     */
    private String senderName;

    /**
     * 发送用户头像
     */
    private String senderAvatar;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 关联的实体ID（如文章ID、评论ID等）
     */
    private Long relatedId;

    /**
     * 跳转链接（可选）
     */
    private String link;

    /**
     * 额外数据（JSON格式，用于存储扩展信息）
     */
    private String extraData;

    /**
     * 创建时间戳
     */
    private Long timestamp;

    /**
     * 创建好友请求通知消息
     * 
     * @param receiverId 接收者ID
     * @param senderId 发送者ID
     * @param senderName 发送者昵称
     * @param senderAvatar 发送者头像
     * @return NotificationMessage对象
     */
    public static NotificationMessage createFriendRequest(
            Long receiverId, Long senderId, String senderName, String senderAvatar) {
        return NotificationMessage.builder()
                .type("FRIEND_REQUEST")
                .receiverId(receiverId)
                .senderId(senderId)
                .senderName(senderName)
                .senderAvatar(senderAvatar)
                .title("好友请求")
                .content(senderName + " 请求添加你为好友")
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建评论通知消息
     * 
     * @param receiverId 接收者ID
     * @param senderId 发送者ID
     * @param senderName 发送者昵称
     * @param articleId 文章ID
     * @param commentContent 评论内容
     * @return NotificationMessage对象
     */
    public static NotificationMessage createComment(
            Long receiverId, Long senderId, String senderName, 
            Long articleId, String commentContent) {
        return NotificationMessage.builder()
                .type("COMMENT")
                .receiverId(receiverId)
                .senderId(senderId)
                .senderName(senderName)
                .title("新评论")
                .content(senderName + " 评论了你的文章")
                .relatedId(articleId)
                .link("/article/" + articleId)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建点赞通知消息
     * 
     * @param receiverId 接收者ID
     * @param senderId 发送者ID
     * @param senderName 发送者昵称
     * @param articleId 文章ID
     * @return NotificationMessage对象
     */
    public static NotificationMessage createLike(
            Long receiverId, Long senderId, String senderName, Long articleId) {
        return NotificationMessage.builder()
                .type("LIKE")
                .receiverId(receiverId)
                .senderId(senderId)
                .senderName(senderName)
                .title("点赞")
                .content(senderName + " 赞了你的文章")
                .relatedId(articleId)
                .link("/article/" + articleId)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}