package com.iwan.blog.service;

import com.iwan.blog.dto.MessageDTO;
import com.iwan.blog.entity.Message;

import java.util.List;

public interface MessageService {

    /**
     * 发送消息
     */
    Message sendMessage(MessageDTO dto, Long userId);

    /**
     * 获取与某个用户的聊天记录
     */
    List<Message> getChatHistory(Long userId, Long targetUserId, int limit, long offset);

    /**
     * 获取未读消息数量
     */
    long getUnreadCount(Long userId);

    /**
     * 标记消息为已读
     */
    void markAsRead(Long userId, Long targetUserId);

    /**
     * 获取聊天会话列表
     */
    List<Message> getChatSessions(Long userId);
}
