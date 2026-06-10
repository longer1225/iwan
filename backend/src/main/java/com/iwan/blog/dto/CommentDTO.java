package com.iwan.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentDTO {

    @NotNull(message = "文章ID不能为空")
    private String articleId;

    private String parentId;

    @NotBlank(message = "内容不能为空")
    private String content;

    private Boolean anonymous = false;

    private String commentExt;
}
