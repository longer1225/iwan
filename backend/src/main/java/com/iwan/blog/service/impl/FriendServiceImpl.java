package com.iwan.blog.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.iwan.blog.dto.FriendGroupDTO;
import com.iwan.blog.dto.FriendRequestDTO;
import com.iwan.blog.dto.NotificationMessage;
import com.iwan.blog.entity.Friend;
import com.iwan.blog.entity.User;
import com.iwan.blog.mapper.FriendMapper;
import com.iwan.blog.mapper.UserMapper;
import com.iwan.blog.service.FriendService;
import com.iwan.blog.service.NotificationProducer;
import com.iwan.blog.util.JsonbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 好友服务实现类
 * 
 * <p>该类实现了FriendService接口，提供好友关系的CRUD操作。
 * 通过依赖注入JsonbUtils和NotificationProducer，解耦了JSONB操作和消息通知。
 * 
 * <p>核心功能：
 * <ul>
 *   <li>发送好友请求：写入数据库 + 发送MQ通知</li>
 *   <li>接受/拒绝好友请求</li>
 *   <li>好友列表管理</li>
 *   <li>好友分组管理</li>
 * </ul>
 */
@Service
public class FriendServiceImpl implements FriendService {

    private static final Logger logger = LoggerFactory.getLogger(FriendServiceImpl.class);

    /**
     * 好友数据访问层
     */
    private final FriendMapper friendMapper;
    
    /**
     * JSONB操作工具类（解耦JSON处理逻辑）
     */
    private final JsonbUtils jsonbUtils;
    
    /**
     * 消息生产者服务（发送异步通知）
     */
    private final NotificationProducer notificationProducer;
    
    /**
     * 用户数据访问层（获取发送者信息）
     */
    private final UserMapper userMapper;

    /**
     * 构造函数依赖注入
     * 
     * @param friendMapper 好友Mapper
     * @param jsonbUtils JSONB工具类
     * @param notificationProducer 消息生产者
     * @param userMapper 用户Mapper
     */
    public FriendServiceImpl(FriendMapper friendMapper, JsonbUtils jsonbUtils,
                            NotificationProducer notificationProducer, UserMapper userMapper) {
        this.friendMapper = friendMapper;
        this.jsonbUtils = jsonbUtils;
        this.notificationProducer = notificationProducer;
        this.userMapper = userMapper;
    }

    /**
     * 发送好友请求
     * 
     * <p>流程：
     * <ol>
     *   <li>检查是否已经是好友</li>
     *   <li>检查是否已发送过请求</li>
     *   <li>写入数据库</li>
     *   <li>发送MQ异步通知给接收者</li>
     * </ol>
     * 
     * @param dto 好友请求数据
     * @param userId 发送者ID
     * @return 创建的好友请求实体
     * @throws RuntimeException 已是好友或已发送请求时抛出异常
     */
    @Override
    @Transactional
    public Friend sendRequest(FriendRequestDTO dto, Long userId) {
        logger.info("=== [FriendService] 开始处理发送好友请求 ===");
        logger.info("[FriendService] 发送者ID: {}", userId);
        logger.info("[FriendService] 目标用户ID(字符串): {}", dto.getTargetUserId());
        logger.info("[FriendService] 验证消息: {}", dto.getMessage());
        
        // 将前端传来的字符串ID转换为Long，避免JavaScript精度丢失
        Long targetUserId;
        try {
            targetUserId = Long.parseLong(dto.getTargetUserId());
            logger.info("[FriendService] 目标用户ID转换为Long: {}", targetUserId);
        } catch (NumberFormatException e) {
            logger.error("[FriendService] 目标用户ID格式错误: {}", dto.getTargetUserId());
            throw new RuntimeException("目标用户ID格式错误");
        }
        
        // 检查是否已经是好友
        logger.info("[FriendService] 检查是否已经是好友...");
        if (isFriend(userId, targetUserId)) {
            logger.warn("[FriendService] 用户 {} 和 {} 已经是好友关系", userId, targetUserId);
            throw new RuntimeException("已经是好友关系");
        }
        logger.info("[FriendService] 不是好友，继续处理");

        // 检查是否已发送过请求
        logger.info("[FriendService] 检查是否已发送过请求...");
        List<Friend> existing = friendMapper.selectList(
            Wrappers.<Friend>lambdaQuery()
                .eq(Friend::getIsDeleted, false)
                .apply("doc->>'type' = 'REQUEST' AND (doc->>'fromUserId')::bigint = " + userId + " AND (doc->>'toUserId')::bigint = " + targetUserId)
        );
        logger.info("[FriendService] 已存在的请求数量: {}", existing.size());
        if (!existing.isEmpty()) {
            logger.warn("[FriendService] 用户 {} 已经向 {} 发送过好友申请", userId, targetUserId);
            throw new RuntimeException("已发送过好友申请");
        }

        // 构建好友请求文档
        logger.info("[FriendService] 构建好友请求文档...");
        Friend friend = new Friend();
        Map<String, Object> doc = new HashMap<>();
        doc.put("type", "REQUEST");
        doc.put("fromUserId", userId);
        doc.put("toUserId", targetUserId);
        doc.put("status", "PENDING");
        doc.put("message", dto.getMessage());
        
        // 使用JsonbUtils序列化
        String docJson = jsonbUtils.toJson(doc);
        logger.info("[FriendService] 文档JSON: {}", docJson);
        friend.setDoc(docJson);
        
        logger.info("[FriendService] 插入数据库...");
        friendMapper.insert(friend);
        logger.info("[FriendService] 数据库插入成功，ID: {}", friend.getId());

        // 发送MQ异步通知给接收者
        try {
            logger.info("[FriendService] 准备发送MQ通知...");
            // 获取发送者信息
            User sender = userMapper.selectById(userId);
            if (sender != null) {
                Map<String, Object> senderDoc = jsonbUtils.parseJson(sender.getDoc());
                String senderName = jsonbUtils.getStringValue(senderDoc, "nickname");
                String senderAvatar = jsonbUtils.getStringValue(senderDoc, "avatar");
                
                // 创建通知消息
                NotificationMessage notification = NotificationMessage.createFriendRequest(
                        targetUserId, userId, senderName, senderAvatar);
                
                // 发送到消息队列
                notificationProducer.sendFriendRequestNotification(notification);
                logger.info("[FriendService] 好友请求通知已发送到MQ - 接收者: {}, 发送者: {}", 
                           targetUserId, userId);
            } else {
                logger.warn("[FriendService] 发送者用户 {} 不存在", userId);
            }
        } catch (Exception e) {
            logger.error("[FriendService] 发送好友请求通知失败 - 接收者: {}, 发送者: {}", 
                        targetUserId, userId, e);
            // 通知发送失败不影响主流程，好友请求已成功写入数据库
        }

        logger.info("=== [FriendService] 发送好友请求处理完成，ID: {} ===", friend.getId());
        return friend;
    }

    /**
     * 接受好友请求
     * 
     * <p>流程：
     * <ol>
     *   <li>验证请求状态</li>
     *   <li>更新请求状态为ACCEPTED</li>
     *   <li>创建双向好友关系</li>
     * </ol>
     * 
     * @param requestId 请求ID
     * @param userId 接收者ID
     * @return 更新后的请求实体
     * @throws RuntimeException 请求不存在或状态无效时抛出异常
     */
    @Override
    @Transactional
    public Friend acceptRequest(Long requestId, Long userId) {
        Friend request = friendMapper.selectById(requestId);
        if (request == null || request.getIsDeleted()) {
            throw new RuntimeException("请求不存在");
        }

        // 使用JsonbUtils解析文档
        Map<String, Object> doc = jsonbUtils.parseJson(request.getDoc());
        Long fromUserId = jsonbUtils.getLongValue(doc, "fromUserId");

        if (!"REQUEST".equals(doc.get("type")) || !"PENDING".equals(doc.get("status"))) {
            throw new RuntimeException("无效的请求状态");
        }

        // 更新请求状态为已同意
        doc.put("status", "ACCEPTED");
        request.setDoc(jsonbUtils.toJson(doc));
        friendMapper.updateById(request);

        // 创建双向好友关系记录
        Friend friend1 = new Friend();
        Map<String, Object> friendDoc1 = new HashMap<>();
        friendDoc1.put("type", "FRIEND");
        friendDoc1.put("userId", userId);
        friendDoc1.put("friendId", fromUserId);
        friendDoc1.put("groupId", null);
        friend1.setDoc(jsonbUtils.toJson(friendDoc1));
        friendMapper.insert(friend1);

        Friend friend2 = new Friend();
        Map<String, Object> friendDoc2 = new HashMap<>();
        friendDoc2.put("type", "FRIEND");
        friendDoc2.put("userId", fromUserId);
        friendDoc2.put("friendId", userId);
        friendDoc2.put("groupId", null);
        friend2.setDoc(jsonbUtils.toJson(friendDoc2));
        friendMapper.insert(friend2);

        logger.info("好友请求已接受 - 请求ID: {}, 用户: {}, 好友: {}", 
                   requestId, userId, fromUserId);

        return request;
    }

    /**
     * 拒绝好友请求
     * 
     * @param requestId 请求ID
     * @param userId 接收者ID
     * @throws RuntimeException 请求不存在或状态无效时抛出异常
     */
    @Override
    @Transactional
    public void rejectRequest(Long requestId, Long userId) {
        Friend request = friendMapper.selectById(requestId);
        if (request == null || request.getIsDeleted()) {
            throw new RuntimeException("请求不存在");
        }

        // 使用JsonbUtils解析文档
        Map<String, Object> doc = jsonbUtils.parseJson(request.getDoc());

        if (!"REQUEST".equals(doc.get("type")) || !"PENDING".equals(doc.get("status"))) {
            throw new RuntimeException("无效的请求状态");
        }

        doc.put("status", "REJECTED");
        request.setDoc(jsonbUtils.toJson(doc));
        friendMapper.updateById(request);
        
        logger.info("好友请求已拒绝 - 请求ID: {}, 用户: {}", requestId, userId);
    }

    /**
     * 获取待处理的好友请求列表
     * 
     * @param userId 用户ID
     * @return 待处理的好友请求列表
     */
    @Override
    public List<Friend> getPendingRequests(Long userId) {
        return friendMapper.selectList(
            Wrappers.<Friend>lambdaQuery()
                .eq(Friend::getIsDeleted, false)
                .apply("doc->>'type' = 'REQUEST' AND (doc->>'toUserId')::bigint = " + userId + " AND doc->>'status' = 'PENDING'")
        );
    }

    /**
     * 获取好友列表
     * 
     * @param userId 用户ID
     * @return 好友列表
     */
    @Override
    public List<Friend> getFriends(Long userId) {
        return friendMapper.selectList(
            Wrappers.<Friend>lambdaQuery()
                .eq(Friend::getIsDeleted, false)
                .apply("doc->>'type' = 'FRIEND' AND (doc->>'userId')::bigint = " + userId)
        );
    }

    /**
     * 删除好友
     * 
     * <p>删除双向好友关系。
     * 
     * @param friendId 好友记录ID
     * @param userId 当前用户ID
     * @throws RuntimeException 好友不存在时抛出异常
     */
    @Override
    @Transactional
    public void deleteFriend(Long friendId, Long userId) {
        Friend friend = friendMapper.selectById(friendId);
        if (friend == null || friend.getIsDeleted()) {
            throw new RuntimeException("好友不存在");
        }

        // 使用JsonbUtils解析文档
        Map<String, Object> doc = jsonbUtils.parseJson(friend.getDoc());
        Long targetFriendId = jsonbUtils.getLongValue(doc, "friendId");

        // 删除自己的好友记录
        friend.setIsDeleted(true);
        friendMapper.updateById(friend);

        // 删除对方的好友记录
        List<Friend> targetFriend = friendMapper.selectList(
            Wrappers.<Friend>lambdaQuery()
                .eq(Friend::getIsDeleted, false)
                .apply("doc->>'type' = 'FRIEND'")
                .apply("(doc->>'userId')::bigint = {0}", targetFriendId)
                .apply("(doc->>'friendId')::bigint = {1}", userId)
        );
        if (!targetFriend.isEmpty()) {
            Friend f = targetFriend.get(0);
            f.setIsDeleted(true);
            friendMapper.updateById(f);
        }
        
        logger.info("好友已删除 - 用户: {}, 好友: {}", userId, targetFriendId);
    }

    /**
     * 创建好友分组
     * 
     * @param dto 分组数据
     * @param userId 用户ID
     * @return 创建的分组实体
     */
    @Override
    @Transactional
    public Friend createGroup(FriendGroupDTO dto, Long userId) {
        Friend group = new Friend();
        Map<String, Object> doc = new HashMap<>();
        doc.put("type", "GROUP");
        doc.put("userId", userId);
        doc.put("name", dto.getName());
        doc.put("description", dto.getDescription());

        group.setDoc(jsonbUtils.toJson(doc));
        friendMapper.insert(group);
        return group;
    }

    /**
     * 获取好友分组列表
     * 
     * @param userId 用户ID
     * @return 分组列表
     */
    @Override
    public List<Friend> getGroups(Long userId) {
        return friendMapper.selectList(
            Wrappers.<Friend>lambdaQuery()
                .eq(Friend::getIsDeleted, false)
                .apply("doc->>'type' = 'GROUP'")
                .apply("(doc->>'userId')::bigint = {0}", userId)
        );
    }

    /**
     * 将好友移动到指定分组
     * 
     * @param friendId 好友记录ID
     * @param groupId 分组ID
     * @param userId 当前用户ID
     * @return 更新后的好友记录
     * @throws RuntimeException 好友不存在时抛出异常
     */
    @Override
    @Transactional
    public Friend moveToGroup(Long friendId, Long groupId, Long userId) {
        Friend friend = friendMapper.selectById(friendId);
        if (friend == null || friend.getIsDeleted()) {
            throw new RuntimeException("好友不存在");
        }

        // 使用JsonbUtils解析文档
        Map<String, Object> doc = jsonbUtils.parseJson(friend.getDoc());
        doc.put("groupId", groupId);
        friend.setDoc(jsonbUtils.toJson(doc));
        friendMapper.updateById(friend);
        return friend;
    }

    /**
     * 检查两个用户是否为好友
     * 
     * @param userId 用户ID
     * @param targetUserId 目标用户ID
     * @return 是否为好友
     */
    @Override
    public boolean isFriend(Long userId, Long targetUserId) {
        List<Friend> friends = friendMapper.selectList(
            Wrappers.<Friend>lambdaQuery()
                .eq(Friend::getIsDeleted, false)
                .apply("doc->>'type' = 'FRIEND' AND (doc->>'userId')::bigint = " + userId + " AND (doc->>'friendId')::bigint = " + targetUserId)
        );
        return !friends.isEmpty();
    }
}