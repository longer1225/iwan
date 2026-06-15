package com.iwan.blog.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iwan.blog.dto.ArticleDTO;
import com.iwan.blog.entity.Article;
import com.iwan.blog.entity.User;
import com.iwan.blog.mapper.UserMapper;
import com.iwan.blog.service.ArticleService;
import com.iwan.blog.vo.PageVO;
import com.iwan.blog.vo.ResponseVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/articles")
public class ArticleController {

    private final ArticleService articleService;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;
    private final com.iwan.blog.service.PermissionService permissionService;

    public ArticleController(ArticleService articleService, UserMapper userMapper, ObjectMapper objectMapper,
                            com.iwan.blog.service.PermissionService permissionService) {
        this.articleService = articleService;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
        this.permissionService = permissionService;
    }

    @GetMapping
    public ResponseVO<PageVO<Map<String, Object>>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String tagId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "time") String sortBy) {

        // 获取当前用户ID
        Long userId = null;
        try {
            String userIdStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            userId = Long.parseLong(userIdStr);
        } catch (Exception e) {
            // 匿名用户
        }

        Page<Article> page = new Page<>(pageNum, pageSize);
        IPage<Article> result = articleService.list(page, keyword, categoryId, tagId, status, sortBy, userId);

        // 转换文章列表为响应格式
        java.util.List<Map<String, Object>> records = new java.util.ArrayList<>();
        for (Article article : result.getRecords()) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", article.getId().toString());  // 转换为字符串避免JS精度问题
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
    public ResponseVO<Map<String, Object>> detail(@PathVariable String id) {
        if (id == null || id.trim().isEmpty() || "undefined".equals(id)) {
            return ResponseVO.badRequest("文章ID无效");
        }
        
        Long articleId;
        try {
            articleId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            return ResponseVO.badRequest("文章ID格式错误");
        }
        
        Article article = articleService.getById(articleId);
        if (article == null) {
            return ResponseVO.notFound("文章不存在");
        }
        
        // 权限检查
        Long userId = null;
        try {
            String userIdStr = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            userId = Long.parseLong(userIdStr);
        } catch (Exception e) {
            // 匿名用户
        }
        
        if (!permissionService.canViewArticle(article, userId)) {
            return ResponseVO.forbidden("无权限查看该文章");
        }

        // 增加阅读计数
        articleService.updateReadCount(articleId);
        
        // 更新计数后重新获取文章以获取最新数据
        article = articleService.getById(articleId);

        Map<String, Object> result = new HashMap<>();
        result.put("id", article.getId());
        result.put("title", article.getDoc() != null ? extractFromDoc(article.getDoc(), "title") : "");
        result.put("content", article.getDoc() != null ? extractFromDoc(article.getDoc(), "content") : "");
        result.put("summary", article.getDoc() != null ? extractFromDoc(article.getDoc(), "summary") : "");
        result.put("cover", article.getDoc() != null ? extractFromDoc(article.getDoc(), "cover") : "");
        
        // 处理作者信息
        String authorName = "";
        String authorAvatar = "";
        
        if (article.getDoc() != null) {
            try {
                Map<String, Object> docMap = objectMapper.readValue(article.getDoc(), new TypeReference<Map<String, Object>>() {});
                
                // 检查是否匿名发布
                Boolean isAnonymous = docMap.containsKey("anonymous") ? (Boolean) docMap.get("anonymous") : false;
                
                if (Boolean.TRUE.equals(isAnonymous)) {
                    // 匿名发布，使用匿名用户信息
                    authorName = "匿名用户";
                    authorAvatar = "";
                } else {
                    // 非匿名发布，获取作者信息
                    Object authorIdObj = docMap.get("authorId");
                    if (authorIdObj != null) {
                        Long authorId = null;
                        if (authorIdObj instanceof Long) {
                            authorId = (Long) authorIdObj;
                        } else if (authorIdObj instanceof Integer) {
                            authorId = ((Integer) authorIdObj).longValue();
                        } else if (authorIdObj instanceof String) {
                            authorId = Long.parseLong((String) authorIdObj);
                        } else if (authorIdObj instanceof Number) {
                            authorId = ((Number) authorIdObj).longValue();
                        }
                        
                        if (authorId != null) {
                            User author = userMapper.selectById(authorId);
                            if (author != null && author.getDoc() != null) {
                                Map<String, Object> authorDoc = objectMapper.readValue(author.getDoc(), new TypeReference<Map<String, Object>>() {});
                                authorName = (String) authorDoc.get("nickname");
                                authorAvatar = (String) authorDoc.get("avatar");
                            }
                        }
                    }
                    
                    // 如果没有获取到作者信息，使用默认值
                    if (authorName == null || authorName.isEmpty()) {
                        authorName = "匿名用户";
                    }
                    if (authorAvatar == null) {
                        authorAvatar = "";
                    }
                }
            } catch (Exception e) {
                authorName = "匿名用户";
                authorAvatar = "";
            }
        } else {
            authorName = "匿名用户";
            authorAvatar = "";
        }
        
        result.put("authorName", authorName);
        result.put("authorAvatar", authorAvatar);
        result.put("readCount", article.getDoc() != null ? getIntFromDoc(article.getDoc(), "readCount", 0) : 0);
        result.put("likeCount", article.getDoc() != null ? getIntFromDoc(article.getDoc(), "likeCount", 0) : 0);
        result.put("collectCount", article.getDoc() != null ? getIntFromDoc(article.getDoc(), "collectCount", 0) : 0);
        result.put("commentCount", article.getDoc() != null ? getIntFromDoc(article.getDoc(), "commentCount", 0) : 0);
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
            item.put("id", article.getId().toString());  // 转换为字符串避免JS精度问题
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
