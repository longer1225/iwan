package com.iwan.blog.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.iwan.blog.handler.JsonbTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_tag")
public class Tag extends BaseEntity {

    @TableField(value = "doc", typeHandler = JsonbTypeHandler.class)
    private String doc;
}