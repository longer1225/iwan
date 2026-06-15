package com.iwan.blog.controller;

import com.iwan.blog.entity.Article;
import com.iwan.blog.entity.User;
import com.iwan.blog.mapper.ArticleMapper;
import com.iwan.blog.mapper.UserMapper;
import com.iwan.blog.vo.ResponseVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    public SearchController(ArticleMapper articleMapper, UserMapper userMapper, ObjectMapper objectMapper) {
        this.articleMapper = articleMapper;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseVO<Map<String, Object>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "all") String type,
            @RequestParam(defaultValue = "10") int limit) {

        Map<String, Object> result = new HashMap<>();
        
        if ("article".equals(type) || "all".equals(type)) {
            List<Article> articles = articleMapper.selectList(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<Article>lambdaQuery()
                    .eq(Article::getIsDeleted, false)
                    .apply("doc->>'status' = 'PUBLISHED'")
                    .and(wrapper -> wrapper
                        .apply("doc->>'title' LIKE {0}", "%" + keyword + "%")
                        .or()
                        .apply("doc->>'summary' LIKE {0}", "%" + keyword + "%")
                        .or()
                        .apply("doc->>'content' LIKE {0}", "%" + keyword + "%")
                    )
                    .orderByDesc(Article::getCreateTime)
                    .last("LIMIT " + limit)
            );

            List<Map<String, Object>> articleResults = articles.stream().map(article -> {
                try {
                    Map<String, Object> doc = objectMapper.readValue(article.getDoc(), new TypeReference<Map<String, Object>>() {});
                    
                    User author = userMapper.selectById(((Number) doc.get("userId")).longValue());
                    String authorName = "匿名用户";
                    String authorAvatar = "";
                    
                    if (author != null && author.getDoc() != null) {
                        Map<String, Object> authorDoc = objectMapper.readValue(author.getDoc(), new TypeReference<Map<String, Object>>() {});
                        authorName = (String) authorDoc.get("nickname");
                        authorAvatar = (String) authorDoc.get("avatar");
                    }

                    Map<String, Object> item = new HashMap<>();
                    item.put("type", "article");
                    item.put("id", article.getId());
                    item.put("title", doc.get("title"));
                    item.put("summary", doc.get("summary"));
                    item.put("cover", doc.get("cover"));
                    item.put("readCount", doc.get("readCount"));
                    item.put("likeCount", doc.get("likeCount"));
                    item.put("commentCount", doc.get("commentCount"));
                    item.put("authorName", authorName);
                    item.put("authorAvatar", authorAvatar);
                    item.put("createTime", article.getCreateTime());
                    return (Map<String, Object>) item;
                } catch (Exception e) {
                    return new HashMap<String, Object>();
                }
            }).collect(Collectors.toList());

            result.put("articles", articleResults);
        }

        if ("user".equals(type) || "all".equals(type)) {
            List<User> users = userMapper.selectList(
                com.baomidou.mybatisplus.core.toolkit.Wrappers.<User>lambdaQuery()
                    .eq(User::getIsDeleted, false)
                    .apply("doc->>'nickname' LIKE {0}", "%" + keyword + "%")
                    .last("LIMIT " + limit)
            );

            List<Map<String, Object>> userResults = users.stream().map(user -> {
                try {
                    Map<String, Object> doc = objectMapper.readValue(user.getDoc(), new TypeReference<Map<String, Object>>() {});
                    
                    Map<String, Object> item = new HashMap<>();
                    item.put("type", "user");
                    item.put("id", String.valueOf(user.getId()));  // 转为字符串避免前端精度丢失
                    item.put("nickname", doc.get("nickname"));
                    item.put("avatar", doc.get("avatar"));
                    item.put("bio", doc.get("bio"));
                    return (Map<String, Object>) item;
                } catch (Exception e) {
                    return new HashMap<String, Object>();
                }
            }).collect(Collectors.toList());

            result.put("users", userResults);
        }

        return ResponseVO.success(result);
    }

    @GetMapping("/hot")
    public ResponseVO<List<Map<String, Object>>> hotKeywords() {
        List<Map<String, Object>> hotKeywords = Arrays.asList(
            new HashMap<String, Object>() {{ put("keyword", "Vue3"); put("count", 1234); }},
            new HashMap<String, Object>() {{ put("keyword", "Java"); put("count", 856); }},
            new HashMap<String, Object>() {{ put("keyword", "SpringBoot"); put("count", 789); }},
            new HashMap<String, Object>() {{ put("keyword", "前端开发"); put("count", 654); }},
            new HashMap<String, Object>() {{ put("keyword", "后端技术"); put("count", 543); }},
            new HashMap<String, Object>() {{ put("keyword", "React"); put("count", 432); }},
            new HashMap<String, Object>() {{ put("keyword", "TypeScript"); put("count", 321); }},
            new HashMap<String, Object>() {{ put("keyword", "数据库"); put("count", 210); }}
        );

        return ResponseVO.success(hotKeywords);
    }
}
