package com.iwan.blog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 好友请求DTO
 * 使用String类型存储targetUserId，避免前端JavaScript数字精度丢失问题
 */
@Data
public class FriendRequestDTO {

    @NotBlank(message = "目标用户ID不能为空")
    private String targetUserId;
    
    private String message;
}
