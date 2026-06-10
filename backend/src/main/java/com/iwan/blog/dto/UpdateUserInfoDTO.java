package com.iwan.blog.dto;

import lombok.Data;

@Data
public class UpdateUserInfoDTO {
    private String nickname;
    private String avatar;
    private String bio;
}
