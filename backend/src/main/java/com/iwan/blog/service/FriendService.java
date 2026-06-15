package com.iwan.blog.service;

import com.iwan.blog.dto.FriendGroupDTO;
import com.iwan.blog.dto.FriendRequestDTO;
import com.iwan.blog.entity.Friend;

import java.util.List;

public interface FriendService {

    /**
     * 发送好友申请
     */
    Friend sendRequest(FriendRequestDTO dto, Long userId);

    /**
     * 同意好友申请
     */
    Friend acceptRequest(Long requestId, Long userId);

    /**
     * 拒绝好友申请
     */
    void rejectRequest(Long requestId, Long userId);

    /**
     * 获取待处理的好友申请列表
     */
    List<Friend> getPendingRequests(Long userId);

    /**
     * 获取好友列表
     */
    List<Friend> getFriends(Long userId);

    /**
     * 删除好友
     */
    void deleteFriend(Long friendId, Long userId);

    /**
     * 创建好友分组
     */
    Friend createGroup(FriendGroupDTO dto, Long userId);

    /**
     * 获取好友分组列表
     */
    List<Friend> getGroups(Long userId);

    /**
     * 将好友移动到分组
     */
    Friend moveToGroup(Long friendId, Long groupId, Long userId);

    /**
     * 检查是否是好友
     */
    boolean isFriend(Long userId, Long targetUserId);
}
