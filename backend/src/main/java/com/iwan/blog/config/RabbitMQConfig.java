package com.iwan.blog.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ消息队列配置类
 * 
 * <p>该类负责配置消息队列、交换机和绑定关系。
 * 用于实现好友请求的异步通知功能。
 * 
 * <p>设计思路：
 * <ul>
 *   <li>使用Direct Exchange（直连交换机）确保消息精确路由</li>
 *   <li>创建持久化队列，确保消息不丢失</li>
 *   <li>使用不同的routing key区分不同类型的通知</li>
 * </ul>
 */
@Configuration
public class RabbitMQConfig {

    /**
     * 交换机名称
     */
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";

    /**
     * 好友请求队列名称
     */
    public static final String FRIEND_REQUEST_QUEUE = "friend.request.queue";

    /**
     * 好友请求路由键
     */
    public static final String FRIEND_REQUEST_ROUTING_KEY = "friend.request";

    /**
     * 系统通知队列名称
     */
    public static final String SYSTEM_NOTIFICATION_QUEUE = "system.notification.queue";

    /**
     * 系统通知路由键
     */
    public static final String SYSTEM_NOTIFICATION_ROUTING_KEY = "system.notification";

    /**
     * 创建通知交换机（Direct类型）
     * 
     * <p>Direct Exchange会根据routing key将消息路由到对应的队列。
     * 
     * @return DirectExchange对象
     */
    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(
                NOTIFICATION_EXCHANGE,
                true,  // durable: 持久化，重启后依然存在
                false  // autoDelete: 不自动删除
        );
    }

    /**
     * 创建好友请求队列
     * 
     * <p>该队列用于存储好友请求通知消息。
     * 
     * @return Queue对象
     */
    @Bean
    public Queue friendRequestQueue() {
        return QueueBuilder.durable(FRIEND_REQUEST_QUEUE).build();
    }

    /**
     * 创建系统通知队列
     * 
     * <p>该队列用于存储系统通知消息（如评论、点赞等）。
     * 
     * @return Queue对象
     */
    @Bean
    public Queue systemNotificationQueue() {
        return QueueBuilder.durable(SYSTEM_NOTIFICATION_QUEUE).build();
    }

    /**
     * 绑定好友请求队列到交换机
     * 
     * <p>使用FRIEND_REQUEST_ROUTING_KEY作为路由键。
     * 
     * @return Binding对象
     */
    @Bean
    public Binding friendRequestBinding() {
        return BindingBuilder.bind(friendRequestQueue())
                .to(notificationExchange())
                .with(FRIEND_REQUEST_ROUTING_KEY);
    }

    /**
     * 绑定系统通知队列到交换机
     * 
     * <p>使用SYSTEM_NOTIFICATION_ROUTING_KEY作为路由键。
     * 
     * @return Binding对象
     */
    @Bean
    public Binding systemNotificationBinding() {
        return BindingBuilder.bind(systemNotificationQueue())
                .to(notificationExchange())
                .with(SYSTEM_NOTIFICATION_ROUTING_KEY);
    }
}