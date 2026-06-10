package com.iwan.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iwan.blog.dto.ArticleDTO;
import com.iwan.blog.entity.Article;
import com.iwan.blog.service.ArticleService;
import com.iwan.blog.vo.PageVO;
import com.iwan.blog.vo.ResponseVO;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/articles")
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    @GetMapping
    public ResponseVO<PageVO<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String tagId,
            @RequestParam(required = false) Integer status) {

        Page<Article> page = new Page<>(pageNum, pageSize);
        IPage<Article> result = articleService.list(page, keyword, categoryId, tagId, status);

        // 转换文章列表为响应格式
        java.util.List<Map<String, Object>> records = new java.util.ArrayList<>();
        for (Article article : result.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", article.getId());
            item.put("title", article.getDoc() != null ? extractFromDoc(article.getDoc(), "title") : "");
            item.put("summary", article.getDoc() != null ? extractFromDoc(article.getDoc(), "summary") : "");
            item.put("cover", article.getDoc() != null ? extractFromDoc(article.getDoc(), "cover") : "");
            item.put("readCount", article.getDoc() != null ? getIntFromDoc(article.getDoc(), "readCount", 0) : 0);
            item.put("likeCount", article.getDoc() != null ? getIntFromDoc(article.getDoc(), "likeCount", 0) : 0);
            item.put("commentCount", article.getDoc() != null ? getIntFromDoc(article.getDoc(), "commentCount", 0) : 0);
            item.put("authorName", article.getDoc() != null ? extractFromDoc(article.getDoc(), "authorName") : "");
            item.put("createTime", article.getCreateTime());
            records.add(item);
        }

        return ResponseVO.success(PageVO.of(result.getTotal(), result.getPages(), 
                result.getCurrent(), result.getSize(), records));
    }

    @GetMapping("/{id}")
    public ResponseVO<Map<String, Object>> detail(@PathVariable Long id) {
        Article article = articleService.getById(id);
        if (article == null) {
            return ResponseVO.notFound("文章不存在");
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", article.getId());
        result.put("title", "文章标题");
        result.put("content", "<p>文章内容</p>");
        result.put("authorName", "作者");
        result.put("authorAvatar", "");
        result.put("categoryName", "分类");
        result.put("tags", new String[]{});
        result.put("readCount", 0);
        result.put("likeCount", 0);
        result.put("collectCount", 0);
        result.put("commentCount", 0);
        result.put("createTime", article.getCreateTime());

        return ResponseVO.success(result);
    }

    @PostMapping
    public ResponseVO<Map<String, Object>> create(@Valid @RequestBody ArticleDTO dto) {
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Article article = articleService.create(dto, Long.parseLong(userId));

        Map<String, Object> result = new HashMap<>();
        result.put("id", article.getId());
        result.put("title", dto.getTitle());

        return ResponseVO.success(result);
    }

    @PutMapping("/{id}")
    public ResponseVO<Map<String, Object>> update(@PathVariable Long id, @Valid @RequestBody ArticleDTO dto) {
        Article article = articleService.update(id, dto);

        Map<String, Object> result = new HashMap<>();
        result.put("id", article.getId());
        result.put("title", dto.getTitle());

        return ResponseVO.success(result);
    }

    @DeleteMapping("/{id}")
    public ResponseVO<Void> delete(@PathVariable Long id) {
        articleService.delete(id);
        return ResponseVO.success();
    }

    @GetMapping("/user")
    public ResponseVO<PageVO<Map<String, Object>>> getUserArticles(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {

        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Page<Article> page = new Page<>(pageNum, pageSize);
        IPage<Article> result = articleService.getUserArticles(page, Long.parseLong(userId));

        // 转换文章列表为响应格式
        java.util.List<Map<String, Object>> records = new java.util.ArrayList<>();
        for (Article article : result.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", article.getId());
            item.put("title", article.getDoc() != null ? extractFromDoc(article.getDoc(), "title") : "");
            item.put("summary", article.getDoc() != null ? extractFromDoc(article.getDoc(), "summary") : "");
            item.put("cover", article.getDoc() != null ? extractFromDoc(article.getDoc(), "cover") : "");
            item.put("readCount", article.getDoc() != null ? getIntFromDoc(article.getDoc(), "readCount", 0) : 0);
            item.put("likeCount", article.getDoc() != null ? getIntFromDoc(article.getDoc(), "likeCount", 0) : 0);
            item.put("commentCount", article.getDoc() != null ? getIntFromDoc(article.getDoc(), "commentCount", 0) : 0);
            item.put("authorName", article.getDoc() != null ? extractFromDoc(article.getDoc(), "authorName") : "");
            item.put("createTime", article.getCreateTime());
            records.add(item);
        }

        return ResponseVO.success(PageVO.of(result.getTotal(), result.getPages(),
                result.getCurrent(), result.getSize(), records));
    }

    // 辅助方法：从doc中提取字符串
    private String extractFromDoc(String doc, String key) {
        if (doc == null || doc.isEmpty()) {
            return "";
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.Map<String, Object> map = mapper.readValue(doc, java.util.Map.class);
            Object value = map.get(key);
            return value != null ? value.toString() : "";
        } catch (Exception e) {
            return "";
        }
    }

    // 辅助方法：从doc中提取整数
    private int getIntFromDoc(String doc, String key, int defaultValue) {
        if (doc == null || doc.isEmpty()) {
            return defaultValue;
        }
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            java.util.Map<String, Object> map = mapper.readValue(doc, java.util.Map.class);
            Object value = map.get(key);
            if (value instanceof Number) {
                return ((Number) value).intValue();
            }
            return defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }
}
