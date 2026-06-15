package com.iwan.blog.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessageDTO {

    @NotNull(message = "接收用户ID不能为空")
    private Long toUserId;
    
    @NotNull(message = "消息内容不能为空")
    private String content;
    
    private String type = "TEXT";
}
