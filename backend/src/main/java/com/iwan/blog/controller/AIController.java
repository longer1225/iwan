package com.iwan.blog.controller;

import com.iwan.blog.entity.Article;
import com.iwan.blog.entity.User;
import com.iwan.blog.mapper.ArticleMapper;
import com.iwan.blog.mapper.UserMapper;
import com.iwan.blog.vo.ResponseVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/ai")
public class AIController {

    private final ArticleMapper articleMapper;
    private final UserMapper userMapper;
    private final ObjectMapper objectMapper;

    public AIController(ArticleMapper articleMapper, UserMapper userMapper, ObjectMapper objectMapper) {
        this.articleMapper = articleMapper;
        this.userMapper = userMapper;
        this.objectMapper = objectMapper;
    }

    /**
     * 获取推荐文章
     */
    @GetMapping("/recommend")
    public ResponseVO<List<Map<String, Object>>> getRecommendations(
            @RequestParam(defaultValue = "5") int limit) {
        
        List<Article> articles = articleMapper.selectList(
            com.baomidou.mybatisplus.core.toolkit.Wrappers.<Article>lambdaQuery()
                .eq(Article::getIsDeleted, false)
                .orderByDesc(Article::getCreateTime)
                .last("LIMIT " + limit)
        );

        List<Map<String, Object>> results = articles.stream().map(article -> {
            try {
                Map<String, Object> doc = objectMapper.readValue(article.getDoc(), new TypeReference<Map<String, Object>>() {});
                
                Long authorId = ((Number) doc.get("userId")).longValue();
                User author = userMapper.selectById(authorId);
                String authorName = "匿名用户";
                String authorAvatar = "";
                
                if (author != null && author.getDoc() != null) {
                    Map<String, Object> authorDoc = objectMapper.readValue(author.getDoc(), new TypeReference<Map<String, Object>>() {});
                    authorName = (String) authorDoc.get("nickname");
                    authorAvatar = (String) authorDoc.get("avatar");
                }

                Map<String, Object> item = new HashMap<>();
                item.put("id", article.getId());
                item.put("title", doc.get("title"));
                item.put("summary", doc.get("summary"));
                item.put("cover", doc.get("cover"));
                item.put("authorName", authorName);
                item.put("authorAvatar", authorAvatar);
                item.put("readCount", doc.get("readCount"));
                item.put("likeCount", doc.get("likeCount"));
                item.put("commentCount", doc.get("commentCount"));
                item.put("createTime", article.getCreateTime());
                return item;
            } catch (Exception e) {
                return new HashMap<String, Object>();
            }
        }).collect(java.util.stream.Collectors.toList());

        return ResponseVO.success(results);
    }

    /**
     * RAG检索 - 根据问题搜索相关文章
     */
    @GetMapping("/rag/search")
    public ResponseVO<List<Map<String, Object>>> ragSearch(
            @RequestParam String query,
            @RequestParam(defaultValue = "3") int limit) {
        
        List<Article> articles = articleMapper.selectList(
            com.baomidou.mybatisplus.core.toolkit.Wrappers.<Article>lambdaQuery()
                .eq(Article::getIsDeleted, false)
                .apply("doc->>'title' LIKE CONCAT('%', {0}, '%') OR doc->>'summary' LIKE CONCAT('%', {0}, '%')", query)
                .last("LIMIT " + limit)
        );

        List<Map<String, Object>> results = articles.stream().map(article -> {
            try {
                Map<String, Object> doc = objectMapper.readValue(article.getDoc(), new TypeReference<Map<String, Object>>() {});
                
                Map<String, Object> item = new HashMap<>();
                item.put("id", article.getId());
                item.put("title", doc.get("title"));
                item.put("summary", doc.get("summary"));
                item.put("content", doc.get("content"));
                return item;
            } catch (Exception e) {
                return new HashMap<String, Object>();
            }
        }).collect(java.util.stream.Collectors.toList());

        return ResponseVO.success(results);
    }

    /**
     * AI聊天接口
     */
    @PostMapping("/chat")
    public ResponseVO<Map<String, Object>> chat(
            @RequestBody Map<String, Object> request) {
        
        String message = (String) request.get("message");
        Boolean useRAG = (Boolean) request.getOrDefault("useRAG", false);
        
        Map<String, Object> response = new HashMap<>();
        response.put("role", "assistant");
        response.put("model", "Iwan AI");
        response.put("time", String.format("%.1f", Math.random() * 2 + 1));
        response.put("tokens", Math.floor(Math.random() * 100 + 50));
        response.put("isRAG", useRAG);

        // 根据关键词生成响应
        String content = generateResponse(message, useRAG);
        response.put("content", content);

        // 如果启用RAG，添加模拟来源
        if (useRAG) {
            List<Map<String, Object>> sources = new ArrayList<>();
            sources.add(Map.of("id", 1, "title", "相关文章1", "excerpt", "这是相关文章的摘要内容..."));
            sources.add(Map.of("id", 2, "title", "相关文章2", "excerpt", "这是另一篇相关文章的摘要..."));
            response.put("sources", sources);
        }

        return ResponseVO.success(response);
    }

    private String generateResponse(String message, boolean useRAG) {
        String ragPrefix = useRAG ? "<p><strong>📚 基于知识库检索结果：</strong></p>" : "";
        
        if (message.contains("写") && (message.contains("文章") || message.contains("博客"))) {
            return ragPrefix + "<p>好的！我来帮你写一篇精彩的文章。为了更好地帮助你，请告诉我：</p><ul><li>文章主题是什么？</li><li>目标读者是谁？</li><li>需要包含哪些关键点？</li></ul><p>或者你可以直接告诉我你的想法，我来帮你组织内容。</p>";
        }
        
        if (message.contains("润色") || message.contains("修改") || message.contains("优化")) {
            return ragPrefix + "<p>没问题！请把需要润色的内容粘贴给我，我会帮你：</p><ul><li>优化语言表达，使其更加流畅自然</li><li>调整文章结构，提升可读性</li><li>修正语法错误和用词不当</li></ul>";
        }
        
        if (message.contains("摘要") || message.contains("总结")) {
            return ragPrefix + "<p>请把需要摘要的文章内容粘贴给我，我会帮你提炼核心观点，生成简明扼要的摘要。</p>";
        }
        
        if (message.toLowerCase().contains("vue")) {
            return ragPrefix + "<p>Vue.js是一个渐进式JavaScript框架，非常适合构建用户界面。</p><p><strong>Vue3的核心特性：</strong></p><ul><li><strong>Composition API</strong>：更灵活的代码组织方式，支持逻辑复用</li><li><strong>响应式系统</strong>：使用Proxy实现，性能更好</li><li><strong>TypeScript支持</strong>：完整的类型定义</li><li><strong>Fragment</strong>：支持多根节点</li><li><strong>Teleport</strong>：组件传送</li></ul>";
        }
        
        if (message.toLowerCase().contains("java") || message.toLowerCase().contains("spring")) {
            return ragPrefix + "<p>Java是一门广泛使用的编程语言，Spring框架是Java生态中最流行的企业级开发框架。</p><p><strong>Spring Boot的优势：</strong></p><ul><li>自动配置，简化开发</li><li>内嵌服务器，一键部署</li><li>丰富的starter依赖</li><li>社区成熟，文档完善</li></ul>";
        }
        
        if (message.toLowerCase().contains("数据库") || message.toLowerCase().contains("sql") || message.toLowerCase().contains("mysql") || message.toLowerCase().contains("postgresql")) {
            return ragPrefix + "<p>数据库是应用程序的核心组件，用于存储和管理数据。</p><p><strong>PostgreSQL的特点：</strong></p><ul><li>开源免费，功能强大</li><li>支持JSONB文档存储</li><li>强大的查询优化器</li><li>支持全文搜索和GIS</li></ul>";
        }
        
        // 默认响应
        return ragPrefix + String.format("<p>感谢你的提问！关于\"%s\"，这是我的回答：</p><p>这是一个很好的话题。根据我的知识库，我可以为你提供相关信息和建议。</p><p>如果你有更具体的问题，欢迎继续提问！</p>", message);
    }

    /**
     * 文章创作建议
     */
    @PostMapping("/write/suggest")
    public ResponseVO<Map<String, Object>> suggestTopics() {
        List<Map<String, Object>> topics = Arrays.asList(
            Map.of("topic", "Vue3 Composition API 实战指南", "description", "深入讲解Vue3的Composition API，包含大量实战案例"),
            Map.of("topic", "Spring Boot 3.x 新特性详解", "description", "介绍Spring Boot 3.0的新功能和最佳实践"),
            Map.of("topic", "PostgreSQL JSONB 应用技巧", "description", "如何在实际项目中使用PostgreSQL的JSONB特性"),
            Map.of("topic", "前端性能优化最佳实践", "description", "提升前端应用性能的实用技巧和策略"),
            Map.of("topic", "微服务架构设计模式", "description", "微服务架构的设计原则和常见模式")
        );

        Map<String, Object> result = new HashMap<>();
        result.put("topics", topics);
        result.put("tips", "选择一个主题开始创作，或者告诉我你想写什么！");
        
        return ResponseVO.success(result);
    }
}
