package com.iwan.blog.service;

import com.iwan.blog.config.RabbitMQConfig;
import com.iwan.blog.dto.NotificationMessage;
import com.iwan.blog.entity.Notification;
import com.iwan.blog.mapper.NotificationMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * 消息消费者服务
 * 
 * <p>该服务负责从RabbitMQ队列消费通知消息，并将通知持久化到数据库。
 * 
 * <p>核心功能：
 * <ul>
 *   <li>消费好友请求通知</li>
 *   <li>消费系统通知（评论、点赞等）</li>
 *   <li>将通知保存到数据库</li>
 *   <li>通过WebSocket实时推送给在线用户</li>
 * </ul>
 * 
 * <p>设计思路：
 * <ul>
 *   <li>使用@RabbitListener注解监听队列</li>
 *   <li>消费成功后将通知写入数据库</li>
 *   <li>异常处理确保消息不丢失</li>
 * </ul>
 */
@Service
public class NotificationConsumer {

    private static final Logger logger = LoggerFactory.getLogger(NotificationConsumer.class);

    /**
     * 通知数据访问层
     */
    private final NotificationMapper notificationMapper;

    /**
     * WebSocket服务（用于实时推送）
     */
    private final WebSocketService webSocketService;

    /**
     * 构造函数依赖注入
     * 
     * @param notificationMapper 通知Mapper
     * @param webSocketService WebSocket服务
     */
    public NotificationConsumer(NotificationMapper notificationMapper, 
                                WebSocketService webSocketService) {
        this.notificationMapper = notificationMapper;
        this.webSocketService = webSocketService;
    }

    /**
     * 消费好友请求通知
     * 
     * <p>该方法监听好友请求队列，当有新消息时：
     * <ol>
     *   <li>将通知保存到数据库</li>
     *   <li>通过WebSocket实时推送给接收者（如果在线）</li>
     * </ol>
     * 
     * @param message 通知消息对象
     */
    @RabbitListener(queues = RabbitMQConfig.FRIEND_REQUEST_QUEUE)
    public void consumeFriendRequestNotification(NotificationMessage message) {
        try {
            logger.info("消费好友请求通知 - 接收者: {}, 发送者: {}", 
                    message.getReceiverId(), message.getSenderId());

            // 1. 将通知保存到数据库
            saveNotificationToDatabase(message);

            // 2. 通过WebSocket实时推送给接收者（如果在线）
            webSocketService.sendNotification(message.getReceiverId(), message);

            logger.info("好友请求通知处理完成 - 接收者: {}", message.getReceiverId());
        } catch (Exception e) {
            logger.error("处理好友请求通知失败 - 接收者: {}", message.getReceiverId(), e);
            // 这里可以添加重试逻辑或死信队列处理
            throw e; // 抛出异常触发消息重试
        }
    }

    /**
     * 消费系统通知（评论、点赞等）
     * 
     * <p>该方法监听系统通知队列，当有新消息时：
     * <ol>
     *   <li>将通知保存到数据库</li>
     *   <li>通过WebSocket实时推送给接收者（如果在线）</li>
     * </ol>
     * 
     * @param message 通知消息对象
     */
    @RabbitListener(queues = RabbitMQConfig.SYSTEM_NOTIFICATION_QUEUE)
    public void consumeSystemNotification(NotificationMessage message) {
        try {
            logger.info("消费系统通知 - 类型: {}, 接收者: {}", 
                    message.getType(), message.getReceiverId());

            // 1. 将通知保存到数据库
            saveNotificationToDatabase(message);

            // 2. 通过WebSocket实时推送给接收者（如果在线）
            webSocketService.sendNotification(message.getReceiverId(), message);

            logger.info("系统通知处理完成 - 类型: {}, 接收者: {}", 
                    message.getType(), message.getReceiverId());
        } catch (Exception e) {
            logger.error("处理系统通知失败 - 类型: {}, 接收者: {}", 
                    message.getType(), message.getReceiverId(), e);
            throw e; // 抛出异常触发消息重试
        }
    }

    /**
     * 将通知保存到数据库
     * 
     * <p>私有方法，用于将NotificationMessage转换为Notification实体并保存。
     * 
     * @param message 通知消息对象
     */
    private void saveNotificationToDatabase(NotificationMessage message) {
        Notification notification = new Notification();
        notification.setUserId(message.getReceiverId());
        notification.setType(message.getType());
        notification.setTitle(message.getTitle());
        notification.setContent(message.getContent());
        notification.setSenderId(message.getSenderId());
        notification.setSenderName(message.getSenderName());
        notification.setSenderAvatar(message.getSenderAvatar());
        notification.setRelatedId(message.getRelatedId());
        notification.setLink(message.getLink());
        notification.setExtraData(message.getExtraData());
        notification.setIsRead(false); // 默认未读

        notificationMapper.insert(notification);
        logger.debug("通知已保存到数据库 - ID: {}, 接收者: {}", 
                notification.getId(), message.getReceiverId());
    }
}