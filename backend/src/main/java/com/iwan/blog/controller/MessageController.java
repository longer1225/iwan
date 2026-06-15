package com.iwan.blog.controller;

import com.iwan.blog.dto.MessageDTO;
import com.iwan.blog.entity.Message;
import com.iwan.blog.entity.User;
import com.iwan.blog.mapper.UserMapper;
import com.iwan.blog.service.MessageService;
import com.iwan.blog.vo.ResponseVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/messages")
public class MessageController {

    private final MessageService messageService;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    public MessageController(MessageService messageService, UserMapper userMapper, ObjectMapper objectMapper) {
        this.messageService = messageService;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public ResponseVO<Map<String, Object>> sendMessage(@Valid @RequestBody MessageDTO dto) {
        String userIdStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = Long.parseLong(userIdStr);

        Message message = messageService.sendMessage(dto, userId);

        Map<String, Object> result = new HashMap<>();
        result.put("id", message.getId());
        result.put("message", "消息发送成功");

        return ResponseVO.success(result);
    }

    @GetMapping("/chat/{targetUserId}")
    public ResponseVO<List<Map<String, Object>>> getChatHistory(
            @PathVariable Long targetUserId,
            @RequestParam(defaultValue = "50") int limit,
            @RequestParam(defaultValue = "0") long offset) {
        
        String userIdStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = Long.parseLong(userIdStr);

        List<Message> messages = messageService.getChatHistory(userId, targetUserId, limit, offset);
        
        messageService.markAsRead(userId, targetUserId);

        List<Map<String, Object>> result = messages.stream().map(message -> {
            try {
                Map<String, Object> doc = objectMapper.readValue(message.getDoc(), new TypeReference<Map<String, Object>>() {});
                Long fromId = ((Number) doc.get("fromUserId")).longValue();
                Long toId = ((Number) doc.get("toUserId")).longValue();
                
                User fromUser = userMapper.selectById(fromId);
                String fromName = "未知用户";
                String fromAvatar = "";
                
                if (fromUser != null && fromUser.getDoc() != null) {
                    Map<String, Object> userDoc = objectMapper.readValue(fromUser.getDoc(), new TypeReference<Map<String, Object>>() {});
                    fromName = (String) userDoc.get("nickname");
                    fromAvatar = (String) userDoc.get("avatar");
                }

                Map<String, Object> item = new HashMap<>();
                item.put("id", message.getId());
                item.put("fromUserId", fromId);
                item.put("toUserId", toId);
                item.put("content", doc.get("content"));
                item.put("type", doc.get("type"));
                item.put("read", doc.get("read"));
                item.put("userName", fromName);
                item.put("userAvatar", fromAvatar);
                item.put("createTime", message.getCreateTime());
                return (Map<String, Object>) item;
            } catch (Exception e) {
                return new HashMap<String, Object>();
            }
        }).collect(Collectors.toList());

        return ResponseVO.success(result);
    }

    @GetMapping("/unread")
    public ResponseVO<Map<String, Object>> getUnreadCount() {
        String userIdStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = Long.parseLong(userIdStr);

        long count = messageService.getUnreadCount(userId);

        return ResponseVO.success(Map.of("count", count));
    }

    @PostMapping("/read/{targetUserId}")
    public ResponseVO<Void> markAsRead(@PathVariable Long targetUserId) {
        String userIdStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = Long.parseLong(userIdStr);

        messageService.markAsRead(userId, targetUserId);

        return ResponseVO.success();
    }

    @GetMapping("/sessions")
    public ResponseVO<List<Map<String, Object>>> getChatSessions() {
        String userIdStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = Long.parseLong(userIdStr);

        List<Message> messages = messageService.getChatSessions(userId);

        List<Map<String, Object>> result = messages.stream().map(message -> {
            try {
                Map<String, Object> doc = objectMapper.readValue(message.getDoc(), new TypeReference<Map<String, Object>>() {});
                Long fromId = ((Number) doc.get("fromUserId")).longValue();
                Long toId = ((Number) doc.get("toUserId")).longValue();
                
                Long targetId = userId.equals(fromId) ? toId : fromId;
                User targetUser = userMapper.selectById(targetId);
                
                String targetName = "未知用户";
                String targetAvatar = "";
                
                if (targetUser != null && targetUser.getDoc() != null) {
                    Map<String, Object> userDoc = objectMapper.readValue(targetUser.getDoc(), new TypeReference<Map<String, Object>>() {});
                    targetName = (String) userDoc.get("nickname");
                    targetAvatar = (String) userDoc.get("avatar");
                }

                Map<String, Object> item = new HashMap<>();
                item.put("id", message.getId());
                item.put("targetUserId", targetId);
                item.put("userName", targetName);
                item.put("userAvatar", targetAvatar);
                item.put("lastMessage", doc.get("content"));
                item.put("createTime", message.getCreateTime());
                return (Map<String, Object>) item;
            } catch (Exception e) {
                return new HashMap<String, Object>();
            }
        }).collect(Collectors.toList());

        return ResponseVO.success(result);
    }
}
