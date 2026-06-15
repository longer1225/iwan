package com.iwan.blog.controller;

import com.iwan.blog.dto.FriendGroupDTO;
import com.iwan.blog.dto.FriendRequestDTO;
import com.iwan.blog.entity.Friend;
import com.iwan.blog.entity.User;
import com.iwan.blog.mapper.UserMapper;
import com.iwan.blog.service.FriendService;
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
@RequestMapping("/api/v1/friends")
public class FriendController {

    private final FriendService friendService;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    public FriendController(FriendService friendService, UserMapper userMapper, ObjectMapper objectMapper) {
        this.friendService = friendService;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/request")
    public ResponseVO<Map<String, Object>> sendRequest(@Valid @RequestBody FriendRequestDTO dto) {
        // 获取当前用户ID
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal == null || "anonymousUser".equals(principal)) {
            return ResponseVO.error(401, "请先登录");
        }
        
        Long userId;
        try {
            userId = Long.parseLong(String.valueOf(principal));
        } catch (NumberFormatException e) {
            return ResponseVO.error(401, "用户身份验证失败");
        }

        try {
            Friend friend = friendService.sendRequest(dto, userId);

            Map<String, Object> result = new HashMap<>();
            result.put("id", String.valueOf(friend.getId()));  // 转为字符串避免前端精度丢失
            result.put("message", "好友申请已发送");

            return ResponseVO.success(result);
        } catch (RuntimeException e) {
            // 根据异常消息返回相应的业务错误码
            if ("已经是好友关系".equals(e.getMessage())) {
                return ResponseVO.error(10404, "已经是好友");
            } else if ("已发送过好友申请".equals(e.getMessage())) {
                return ResponseVO.error(10402, "已发送好友请求");
            } else {
                return ResponseVO.error(10700, e.getMessage());
            }
        }
    }

    @PostMapping("/request/{id}/accept")
    public ResponseVO<Map<String, Object>> acceptRequest(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseVO.error(401, "请先登录");
        }

        friendService.acceptRequest(id, userId);

        return ResponseVO.success(Map.of("message", "已同意好友申请"));
    }

    @PostMapping("/request/{id}/reject")
    public ResponseVO<Map<String, Object>> rejectRequest(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseVO.error(401, "请先登录");
        }

        friendService.rejectRequest(id, userId);

        return ResponseVO.success(Map.of("message", "已拒绝好友申请"));
    }

    @GetMapping("/requests")
    public ResponseVO<List<Map<String, Object>>> getPendingRequests() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseVO.error(401, "请先登录");
        }

        List<Friend> requests = friendService.getPendingRequests(userId);

        List<Map<String, Object>> result = requests.stream().map(request -> {
            try {
                Map<String, Object> doc = objectMapper.readValue(request.getDoc(), new TypeReference<Map<String, Object>>() {});
                Long fromUserId = ((Number) doc.get("fromUserId")).longValue();
                
                User user = userMapper.selectById(fromUserId);
                String nickname = "未知用户";
                String avatar = "";
                
                if (user != null && user.getDoc() != null) {
                    Map<String, Object> userDoc = objectMapper.readValue(user.getDoc(), new TypeReference<Map<String, Object>>() {});
                    nickname = (String) userDoc.get("nickname");
                    avatar = (String) userDoc.get("avatar");
                }

                Map<String, Object> item = new HashMap<>();
                item.put("id", String.valueOf(request.getId()));  // 转换为字符串避免前端精度丢失
                item.put("fromUserId", String.valueOf(fromUserId));  // 转换为字符串避免前端精度丢失
                item.put("userName", nickname);
                item.put("userAvatar", avatar);
                item.put("message", doc.get("message"));
                item.put("createTime", request.getCreateTime());
                return (Map<String, Object>) item;
            } catch (Exception e) {
                return new HashMap<String, Object>();
            }
        }).collect(Collectors.toList());

        return ResponseVO.success(result);
    }

    @GetMapping
    public ResponseVO<List<Map<String, Object>>> getFriends() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseVO.error(401, "请先登录");
        }

        List<Friend> friends = friendService.getFriends(userId);

        List<Map<String, Object>> result = friends.stream().map(friend -> {
            try {
                Map<String, Object> doc = objectMapper.readValue(friend.getDoc(), new TypeReference<Map<String, Object>>() {});
                Long friendId = ((Number) doc.get("friendId")).longValue();
                
                User user = userMapper.selectById(friendId);
                String nickname = "未知用户";
                String avatar = "";
                
                if (user != null && user.getDoc() != null) {
                    Map<String, Object> userDoc = objectMapper.readValue(user.getDoc(), new TypeReference<Map<String, Object>>() {});
                    nickname = (String) userDoc.get("nickname");
                    avatar = (String) userDoc.get("avatar");
                }

                Map<String, Object> item = new HashMap<>();
                item.put("id", friend.getId());
                item.put("friendId", friendId);
                item.put("userName", nickname);
                item.put("userAvatar", avatar);
                item.put("groupId", doc.get("groupId"));
                return (Map<String, Object>) item;
            } catch (Exception e) {
                return new HashMap<String, Object>();
            }
        }).collect(Collectors.toList());

        return ResponseVO.success(result);
    }

    @DeleteMapping("/{id}")
    public ResponseVO<Void> deleteFriend(@PathVariable Long id) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseVO.error(401, "请先登录");
        }

        friendService.deleteFriend(id, userId);

        return ResponseVO.success();
    }

    @PostMapping("/groups")
    public ResponseVO<Map<String, Object>> createGroup(@Valid @RequestBody FriendGroupDTO dto) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseVO.error(401, "请先登录");
        }

        Friend group = friendService.createGroup(dto, userId);

        Map<String, Object> result = new HashMap<>();
        result.put("id", group.getId());
        result.put("name", dto.getName());

        return ResponseVO.success(result);
    }

    @GetMapping("/groups")
    public ResponseVO<List<Map<String, Object>>> getGroups() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseVO.error(401, "请先登录");
        }

        List<Friend> groups = friendService.getGroups(userId);

        List<Map<String, Object>> result = groups.stream().map(group -> {
            try {
                Map<String, Object> doc = objectMapper.readValue(group.getDoc(), new TypeReference<Map<String, Object>>() {});
                Map<String, Object> item = new HashMap<>();
                item.put("id", group.getId());
                item.put("name", doc.get("name"));
                item.put("description", doc.get("description"));
                return (Map<String, Object>) item;
            } catch (Exception e) {
                return new HashMap<String, Object>();
            }
        }).collect(Collectors.toList());

        return ResponseVO.success(result);
    }

    @PutMapping("/{friendId}/group/{groupId}")
    public ResponseVO<Map<String, Object>> moveToGroup(@PathVariable Long friendId, @PathVariable Long groupId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseVO.error(401, "请先登录");
        }

        friendService.moveToGroup(friendId, groupId, userId);

        return ResponseVO.success(Map.of("message", "已移动到分组"));
    }

    @GetMapping("/is-friend/{targetUserId}")
    public ResponseVO<Boolean> isFriend(@PathVariable Long targetUserId) {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseVO.error(401, "请先登录");
        }

        boolean isFriend = friendService.isFriend(userId, targetUserId);

        return ResponseVO.success(isFriend);
    }

    /**
     * 获取当前登录用户ID
     * 
     * @return 用户ID，如果未登录或验证失败返回null
     */
    private Long getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal == null || "anonymousUser".equals(principal)) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(principal));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
