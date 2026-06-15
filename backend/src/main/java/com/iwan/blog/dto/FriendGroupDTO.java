package com.iwan.blog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FriendGroupDTO {

    @NotBlank(message = "分组名称不能为空")
    private String name;
    
    private String description;
}
