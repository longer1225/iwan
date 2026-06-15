package com.iwan.blog.service;

import com.iwan.blog.config.RabbitMQConfig;
import com.iwan.blog.dto.NotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

/**
 * 消息生产者服务
 * 
 * <p>该服务负责将通知消息发送到RabbitMQ队列，实现异步通知功能。
 * 
 * <p>核心功能：
 * <ul>
 *   <li>发送好友请求通知</li>
 *   <li>发送评论通知</li>
 *   <li>发送点赞通知</li>
 *   <li>发送系统通知</li>
 * </ul>
 * 
 * <p>设计思路：
 * <ul>
 *   <li>使用RabbitTemplate发送消息到指定交换机</li>
 *   <li>通过routing key区分不同类型的消息</li>
 *   <li>记录发送日志便于问题排查</li>
 * </ul>
 */
@Service
public class NotificationProducer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationProducer.class);

    /**
     * RabbitMQ消息模板
     */
    private final RabbitTemplate rabbitTemplate;

    /**
     * 构造函数依赖注入
     * 
     * @param rabbitTemplate RabbitMQ模板
     */
    public NotificationProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * 发送好友请求通知
     * 
     * <p>当用户A发送好友请求给用户B时，调用此方法发送异步通知。
     * 
     * @param message 通知消息对象
     */
    public void sendFriendRequestNotification(NotificationMessage message) {
        try {
            // 发送消息到好友请求队列
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.FRIEND_REQUEST_ROUTING_KEY,
                    message
            );
            logger.info("好友请求通知已发送 - 接收者: {}, 发送者: {}", 
                    message.getReceiverId(), message.getSenderId());
        } catch (Exception e) {
            logger.error("发送好友请求通知失败 - 接收者: {}, 发送者: {}", 
                    message.getReceiverId(), message.getSenderId(), e);
            // 这里可以添加降级逻辑，如直接写入数据库通知表
        }
    }

    /**
     * 发送评论通知
     * 
     * <p>当用户A评论用户B的文章时，调用此方法发送异步通知。
     * 
     * @param message 通知消息对象
     */
    public void sendCommentNotification(NotificationMessage message) {
        try {
            // 发送消息到系统通知队列
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.SYSTEM_NOTIFICATION_ROUTING_KEY,
                    message
            );
            logger.info("评论通知已发送 - 接收者: {}, 发送者: {}", 
                    message.getReceiverId(), message.getSenderId());
        } catch (Exception e) {
            logger.error("发送评论通知失败 - 接收者: {}, 发送者: {}", 
                    message.getReceiverId(), message.getSenderId(), e);
        }
    }

    /**
     * 发送点赞通知
     * 
     * <p>当用户A点赞用户B的文章时，调用此方法发送异步通知。
     * 
     * @param message 通知消息对象
     */
    public void sendLikeNotification(NotificationMessage message) {
        try {
            // 发送消息到系统通知队列
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.SYSTEM_NOTIFICATION_ROUTING_KEY,
                    message
            );
            logger.info("点赞通知已发送 - 接收者: {}, 发送者: {}", 
                    message.getReceiverId(), message.getSenderId());
        } catch (Exception e) {
            logger.error("发送点赞通知失败 - 接收者: {}, 发送者: {}", 
                    message.getReceiverId(), message.getSenderId(), e);
        }
    }

    /**
     * 发送系统通知
     * 
     * <p>用于发送系统级别的通知，如系统公告、安全提醒等。
     * 
     * @param message 通知消息对象
     */
    public void sendSystemNotification(NotificationMessage message) {
        try {
            // 发送消息到系统通知队列
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.SYSTEM_NOTIFICATION_ROUTING_KEY,
                    message
            );
            logger.info("系统通知已发送 - 接收者: {}", message.getReceiverId());
        } catch (Exception e) {
            logger.error("发送系统通知失败 - 接收者: {}", message.getReceiverId(), e);
        }
    }
}