package com.iwan.blog.service;

import com.iwan.blog.dto.NotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket服务类
 * 
 * <p>该服务负责管理WebSocket连接，实现实时消息推送功能。
 * 
 * <p>核心功能：
 * <ul>
 *   <li>管理用户WebSocket会话</li>
 *   <li>向指定用户发送实时通知</li>
 *   <li>广播消息给所有在线用户</li>
 * </ul>
 * 
 * <p>设计思路：
 * <ul>
 *   <li>使用ConcurrentHashMap存储用户ID与会话的映射</li>
 *   <li>支持会话的添加和移除</li>
 *   <li>异常处理确保单个会话失败不影响其他用户</li>
 * </ul>
 */
@Service
public class WebSocketService {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketService.class);

    /**
     * 用户ID与WebSocket会话的映射
     * 
     * <p>使用ConcurrentHashMap保证线程安全，支持高并发场景。
     * Key: 用户ID
     * Value: WebSocketSession对象
     */
    private final Map<Long, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    /**
     * 添加用户会话
     * 
     * <p>当用户建立WebSocket连接时调用，将用户ID与会话绑定。
     * 
     * @param userId 用户ID
     * @param session WebSocket会话
     */
    public void addSession(Long userId, WebSocketSession session) {
        userSessions.put(userId, session);
        logger.info("用户WebSocket会话已添加 - 用户ID: {}, 当前在线用户数: {}", 
                   userId, userSessions.size());
    }

    /**
     * 移除用户会话
     * 
     * <p>当用户断开WebSocket连接时调用，清理会话映射。
     * 
     * @param userId 用户ID
     */
    public void removeSession(Long userId) {
        userSessions.remove(userId);
        logger.info("用户WebSocket会话已移除 - 用户ID: {}, 当前在线用户数: {}", 
                   userId, userSessions.size());
    }

    /**
     * 向指定用户发送通知
     * 
     * <p>将通知消息序列化为JSON格式，通过WebSocket推送给用户。
     * 如果用户不在线，消息会被丢弃（因为消息已通过MQ持久化到数据库）。
     * 
     * @param userId 接收通知的用户ID
     * @param message 通知消息对象
     */
    public void sendNotification(Long userId, NotificationMessage message) {
        WebSocketSession session = userSessions.get(userId);
        if (session == null || !session.isOpen()) {
            logger.debug("用户不在线或会话已关闭，跳过WebSocket推送 - 用户ID: {}", userId);
            return;
        }

        try {
            // 将通知消息转换为JSON字符串
            String jsonMessage = convertToJson(message);
            
            // 发送消息
            session.sendMessage(new TextMessage(jsonMessage));
            logger.info("通知已通过WebSocket推送 - 用户ID: {}, 类型: {}", 
                       userId, message.getType());
        } catch (IOException e) {
            logger.error("WebSocket发送消息失败 - 用户ID: {}", userId, e);
            // 发送失败时移除会话，避免重复尝试
            removeSession(userId);
        }
    }

    /**
     * 广播消息给所有在线用户
     * 
     * <p>将消息发送给所有当前在线的用户。
     * 
     * @param message 消息内容
     */
    public void broadcast(String message) {
        userSessions.forEach((userId, session) -> {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage(message));
                } catch (IOException e) {
                    logger.error("广播消息失败 - 用户ID: {}", userId, e);
                    removeSession(userId);
                }
            }
        });
        logger.info("消息已广播给所有在线用户 - 在线用户数: {}", userSessions.size());
    }

    /**
     * 获取当前在线用户数量
     * 
     * @return 在线用户数量
     */
    public int getOnlineUserCount() {
        return userSessions.size();
    }

    /**
     * 检查用户是否在线
     * 
     * @param userId 用户ID
     * @return 是否在线
     */
    public boolean isUserOnline(Long userId) {
        WebSocketSession session = userSessions.get(userId);
        return session != null && session.isOpen();
    }

    /**
     * 将NotificationMessage对象转换为JSON字符串
     * 
     * <p>私有方法，用于序列化通知消息。
     * 
     * @param message 通知消息对象
     * @return JSON字符串
     */
    private String convertToJson(NotificationMessage message) {
        // 简单实现：手动构建JSON字符串
        // 实际项目中可以使用Jackson或其他JSON库
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"type\":\"").append(message.getType()).append("\",");
        json.append("\"title\":\"").append(message.getTitle()).append("\",");
        json.append("\"content\":\"").append(message.getContent()).append("\",");
        json.append("\"senderId\":").append(message.getSenderId()).append(",");
        json.append("\"senderName\":\"").append(message.getSenderName()).append("\",");
        json.append("\"senderAvatar\":\"").append(message.getSenderAvatar()).append("\",");
        json.append("\"relatedId\":").append(message.getRelatedId()).append(",");
        json.append("\"link\":\"").append(message.getLink() != null ? message.getLink() : "").append("\",");
        json.append("\"timestamp\":").append(message.getTimestamp());
        json.append("}");
        return json.toString();
    }
}