package com.iwan.blog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ArticleDTO {

    @NotBlank(message = "标题不能为空")
    private String title;

    @NotBlank(message = "内容不能为空")
    private String content;

    private String categoryId;

    private List<String> tagList;

    private Integer status = 1;

    private String cover;

    private String articleExt;
    
    private Boolean anonymous = false;
    
    /**
     * 可见权限：
     * PUBLIC - 公开可见
     * FRIENDS_ONLY - 仅好友可见
     * PRIVATE - 仅自己可见
     * GROUP - 指定分组可见
     */
    private String visibility = "PUBLIC";
    
    /**
     * 指定分组ID（当visibility为GROUP时必填）
     */
    private String groupId;
}
