package com.iwan.blog.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwan.blog.entity.Article;
import com.iwan.blog.entity.Tag;
import com.iwan.blog.mapper.ArticleMapper;
import com.iwan.blog.mapper.TagMapper;
import com.iwan.blog.vo.ResponseVO;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/tags")
public class TagController {

    private final TagMapper tagMapper;
    private final ArticleMapper articleMapper;
    private final ObjectMapper objectMapper;

    public TagController(TagMapper tagMapper, ArticleMapper articleMapper, ObjectMapper objectMapper) {
        this.tagMapper = tagMapper;
        this.articleMapper = articleMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 获取所有标签
     */
    @GetMapping
    public ResponseVO<List<Map<String, Object>>> getAllTags() {
        List<Tag> tags = tagMapper.selectList(
            Wrappers.<Tag>lambdaQuery()
                .eq(Tag::getIsDeleted, false)
                .orderByDesc(Tag::getCreateTime)
        );

        List<Map<String, Object>> results = tags.stream().map(tag -> {
            try {
                Map<String, Object> doc = objectMapper.readValue(tag.getDoc(), new TypeReference<Map<String, Object>>() {});
                
                Map<String, Object> item = new HashMap<>();
                item.put("id", tag.getId());
                item.put("name", doc.get("name"));
                item.put("description", doc.get("description"));
                item.put("color", doc.get("color"));
                item.put("createTime", tag.getCreateTime());
                return item;
            } catch (Exception e) {
                return new HashMap<String, Object>();
            }
        }).collect(Collectors.toList());

        return ResponseVO.success(results);
    }

    /**
     * 获取热门标签（按文章数量排序）
     */
    @GetMapping("/hot")
    public ResponseVO<List<Map<String, Object>>> getHotTags(@RequestParam(defaultValue = "10") int limit) {
        List<Tag> tags = tagMapper.selectList(
            Wrappers.<Tag>lambdaQuery()
                .eq(Tag::getIsDeleted, false)
                .orderByDesc(Tag::getCreateTime)
        );

        // 统计每个标签的文章数量
        List<Map<String, Object>> results = tags.stream().map(tag -> {
            try {
                Map<String, Object> doc = objectMapper.readValue(tag.getDoc(), new TypeReference<Map<String, Object>>() {});
                String tagName = (String) doc.get("name");
                
                // 查询使用该标签的文章数量
                long count = articleMapper.selectCount(
                    Wrappers.<Article>lambdaQuery()
                        .eq(Article::getIsDeleted, false)
                        .apply("doc->>'tagList' LIKE CONCAT('%\"{0}\"%')", tagName)
                );

                Map<String, Object> item = new HashMap<>();
                item.put("id", tag.getId());
                item.put("name", tagName);
                item.put("count", count);
                return item;
            } catch (Exception e) {
                return new HashMap<String, Object>();
            }
        }).collect(Collectors.toList());

        // 按文章数量排序
        results.sort((a, b) -> {
            Long countA = (Long) a.getOrDefault("count", 0L);
            Long countB = (Long) b.getOrDefault("count", 0L);
            return countB.compareTo(countA);
        });

        // 返回前N个
        return ResponseVO.success(results.stream().limit(limit).collect(Collectors.toList()));
    }

    /**
     * 创建标签
     */
    @PostMapping
    public ResponseVO<Map<String, Object>> createTag(@RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            String description = (String) request.get("description");
            String color = (String) request.getOrDefault("color", "#409EFF");

            if (name == null || name.trim().isEmpty()) {
                return ResponseVO.error(400, "标签名称不能为空");
            }

            Map<String, Object> doc = new HashMap<>();
            doc.put("name", name);
            doc.put("description", description);
            doc.put("color", color);

            Tag tag = new Tag();
            tag.setDoc(objectMapper.writeValueAsString(doc));
            tagMapper.insert(tag);

            Map<String, Object> result = new HashMap<>();
            result.put("id", tag.getId());
            result.put("name", name);
            result.put("description", description);
            result.put("color", color);

            return ResponseVO.success(result);
        } catch (Exception e) {
            return ResponseVO.error(500, "创建标签失败: " + e.getMessage());
        }
    }

    /**
     * 删除标签
     */
    @DeleteMapping("/{id}")
    public ResponseVO<Void> deleteTag(@PathVariable Long id) {
        try {
            tagMapper.deleteById(id);
            return ResponseVO.success(null);
        } catch (Exception e) {
            return ResponseVO.error(500, "删除标签失败: " + e.getMessage());
        }
    }
}