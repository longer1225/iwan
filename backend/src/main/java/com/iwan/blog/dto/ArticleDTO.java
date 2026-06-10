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
}
