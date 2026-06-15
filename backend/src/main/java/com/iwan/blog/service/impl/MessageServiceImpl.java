package com.iwan.blog.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iwan.blog.dto.MessageDTO;
import com.iwan.blog.entity.Message;
import com.iwan.blog.mapper.MessageMapper;
import com.iwan.blog.service.MessageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MessageServiceImpl implements MessageService {

    private static final Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);

    private final MessageMapper messageMapper;
    private final ObjectMapper objectMapper;

    public MessageServiceImpl(MessageMapper messageMapper, ObjectMapper objectMapper) {
        this.messageMapper = messageMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public Message sendMessage(MessageDTO dto, Long userId) {
        Message message = new Message();
        Map<String, Object> doc = new HashMap<>();
        doc.put("type", dto.getType());
        doc.put("fromUserId", userId);
        doc.put("toUserId", dto.getToUserId());
        doc.put("content", dto.getContent());
        doc.put("read", false);

        try {
            message.setDoc(objectMapper.writeValueAsString(doc));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("序列化失败", e);
        }

        messageMapper.insert(message);
        return message;
    }

    @Override
    public List<Message> getChatHistory(Long userId, Long targetUserId, int limit, long offset) {
        return messageMapper.selectList(
            Wrappers.<Message>lambdaQuery()
                .eq(Message::getIsDeleted, false)
                .apply("((doc->>'fromUserId')::bigint = {0} AND (doc->>'toUserId')::bigint = {1}) OR " +
                       "((doc->>'fromUserId')::bigint = {1} AND (doc->>'toUserId')::bigint = {0})", 
                       userId, targetUserId)
                .orderByDesc(Message::getCreateTime)
                .last("LIMIT " + limit + " OFFSET " + offset)
        );
    }

    @Override
    public long getUnreadCount(Long userId) {
        return messageMapper.selectCount(
            Wrappers.<Message>lambdaQuery()
                .eq(Message::getIsDeleted, false)
                .apply("(doc->>'toUserId')::bigint = {0}", userId)
                .apply("doc->>'read' = 'false'")
        );
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, Long targetUserId) {
        List<Message> messages = messageMapper.selectList(
            Wrappers.<Message>lambdaQuery()
                .eq(Message::getIsDeleted, false)
                .apply("(doc->>'toUserId')::bigint = {0}", userId)
                .apply("(doc->>'fromUserId')::bigint = {1}", targetUserId)
                .apply("doc->>'read' = 'false'")
        );

        for (Message message : messages) {
            try {
                Map<String, Object> doc = objectMapper.readValue(message.getDoc(), new TypeReference<Map<String, Object>>() {});
                doc.put("read", true);
                message.setDoc(objectMapper.writeValueAsString(doc));
                messageMapper.updateById(message);
            } catch (JsonProcessingException e) {
                logger.error("更新消息状态失败", e);
            }
        }
    }

    @Override
    public List<Message> getChatSessions(Long userId) {
        List<Message> messages = messageMapper.selectList(
            Wrappers.<Message>lambdaQuery()
                .eq(Message::getIsDeleted, false)
                .apply("(doc->>'fromUserId')::bigint = {0} OR (doc->>'toUserId')::bigint = {0}", userId)
                .orderByDesc(Message::getCreateTime)
        );

        return messages.stream()
            .filter(m -> {
                try {
                    Map<String, Object> doc = objectMapper.readValue(m.getDoc(), new TypeReference<Map<String, Object>>() {});
                    Long fromId = ((Number) doc.get("fromUserId")).longValue();
                    Long toId = ((Number) doc.get("toUserId")).longValue();
                    return !userId.equals(fromId) || !userId.equals(toId);
                } catch (Exception e) {
                    return false;
                }
            })
            .toList();
    }
}
