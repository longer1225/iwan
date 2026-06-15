
package com.iwan.blog.util;

import com.iwan.blog.entity.Article;
import com.iwan.blog.entity.Comment;
import com.iwan.blog.entity.User;
import com.iwan.blog.mapper.UserMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * VO对象构建器工具类
 * 
 * <p>该工具类负责将Entity实体转换为对外暴露的VO（View Object），包括：
 * <ul>
 *   <li>文章实体转文章VO</li>
 *   <li>评论实体转评论VO</li>
 *   <li>用户实体转用户VO</li>
 * </ul>
 * 
 * <p>设计目的：
 * <ul>
 *   <li>解耦Service层与VO封装逻辑</li>
 *   <li>提供统一的VO构建入口</li>
 *   <li>避免重复的VO封装代码</li>
 * </ul>
 */
@Component
public class VoBuilder {

    private final JsonbUtils jsonbUtils;
    private final UserMapper userMapper;

    public VoBuilder(JsonbUtils jsonbUtils, UserMapper userMapper) {
        this.jsonbUtils = jsonbUtils;
        this.userMapper = userMapper;
    }

    /**
     * 将文章实体转换为VO Map
     * 
     * @param article 文章实体
     * @return 文章VO Map
     */
    public Map<String, Object> buildArticleVo(Article article) {
        Map<String, Object> vo = new HashMap<>();
        
        // 基础字段
        vo.put("id", article.getId());
        vo.put("createTime", article.getCreateTime());
        vo.put("updateTime", article.getUpdateTime());
        
        // JSONB文档字段
        Map<String, Object> doc = jsonbUtils.parseJson(article.getDoc());
        vo.put("title", jsonbUtils.getStringValue(doc, "title"));
        vo.put("content", jsonbUtils.getStringValue(doc, "content"));
        vo.put("categoryId", jsonbUtils.getLongValue(doc, "categoryId"));
        vo.put("tagList", doc.get("tagList"));
        vo.put("status", jsonbUtils.getStringValue(doc, "status"));
        vo.put("cover", jsonbUtils.getStringValue(doc, "cover"));
        vo.put("readCount", jsonbUtils.getLongValue(doc, "readCount"));
        vo.put("likeCount", jsonbUtils.getLongValue(doc, "likeCount"));
        vo.put("collectCount", jsonbUtils.getLongValue(doc, "collectCount"));
        vo.put("commentCount", jsonbUtils.getLongValue(doc, "commentCount"));
        vo.put("visibility", jsonbUtils.getVisibility(doc));
        vo.put("groupId", jsonbUtils.getLongValue(doc, "groupId"));
        
        // 作者信息处理
        String authorName = jsonbUtils.getStringValue(doc, "authorName");
        String authorAvatar = jsonbUtils.getStringValue(doc, "authorAvatar");
        Boolean isAnonymous = jsonbUtils.getBooleanValue(doc, "anonymous");
        
        // 如果是匿名文章，使用匿名信息；否则从数据库查询作者信息
        if (Boolean.TRUE.equals(isAnonymous) || authorName != null && !authorName.isEmpty()) {
            vo.put("authorName", authorName);
            vo.put("authorAvatar", authorAvatar);
            vo.put("anonymous", true);
        } else {
            Long authorId = jsonbUtils.getLongValue(doc, "authorId");
            if (authorId != null) {
                User author = userMapper.selectById(authorId);
                if (author != null) {
                    Map<String, Object> authorDoc = jsonbUtils.parseJson(author.getDoc());
                    vo.put("authorId", authorId);
                    vo.put("authorName", jsonbUtils.getStringValue(authorDoc, "nickname"));
                    vo.put("authorAvatar", jsonbUtils.getStringValue(authorDoc, "avatar"));
                    vo.put("anonymous", false);
                }
            }
        }
        
        return vo;
    }

    /**
     * 将文章列表转换为VO列表
     * 
     * @param articles 文章实体列表
     * @return 文章VO列表
     */
    public List<Map<String, Object>> buildArticleVoList(List<Article> articles) {
        return articles.stream()
                .map(this::buildArticleVo)
                .toList();
    }

    /**
     * 将评论实体转换为VO Map
     * 
     * @param comment 评论实体
     * @return 评论VO Map
     */
    public Map<String, Object> buildCommentVo(Comment comment) {
        Map<String, Object> vo = new HashMap<>();
        
        // 基础字段
        vo.put("id", comment.getId());
        vo.put("createTime", comment.getCreateTime());
        vo.put("updateTime", comment.getUpdateTime());
        
        // JSONB文档字段
        Map<String, Object> doc = jsonbUtils.parseJson(comment.getDoc());
        vo.put("content", jsonbUtils.getStringValue(doc, "content"));
        vo.put("articleId", jsonbUtils.getLongValue(doc, "articleId"));
        vo.put("parentId", jsonbUtils.getLongValue(doc, "parentId"));
        vo.put("likeCount", jsonbUtils.getLongValue(doc, "likeCount"));
        
        // 作者信息
        Long authorId = jsonbUtils.getLongValue(doc, "authorId");
        if (authorId != null) {
            User author = userMapper.selectById(authorId);
            if (author != null) {
                Map<String, Object> authorDoc = jsonbUtils.parseJson(author.getDoc());
                vo.put("authorId", authorId);
                vo.put("authorName", jsonbUtils.getStringValue(authorDoc, "nickname"));
                vo.put("authorAvatar", jsonbUtils.getStringValue(authorDoc, "avatar"));
            }
        }
        
        return vo;
    }

    /**
     * 将评论列表转换为VO列表
     * 
     * @param comments 评论实体列表
     * @return 评论VO列表
     */
    public List<Map<String, Object>> buildCommentVoList(List<Comment> comments) {
        return comments.stream()
                .map(this::buildCommentVo)
                .toList();
    }

    /**
     * 将用户实体转换为VO Map
     * 
     * @param user 用户实体
     * @return 用户VO Map
     */
    public Map<String, Object> buildUserVo(User user) {
        Map<String, Object> vo = new HashMap<>();
        vo.put("id", user.getId());
        
        // 用户信息存储在JSONB字段中
        Map<String, Object> doc = jsonbUtils.parseJson(user.getDoc());
        vo.put("username", jsonbUtils.getStringValue(doc, "username"));
        vo.put("nickname", jsonbUtils.getStringValue(doc, "nickname"));
        vo.put("avatar", jsonbUtils.getStringValue(doc, "avatar"));
        vo.put("bio", jsonbUtils.getStringValue(doc, "bio"));
        vo.put("email", jsonbUtils.getStringValue(doc, "email"));
        vo.put("phone", jsonbUtils.getStringValue(doc, "phone"));
        
        vo.put("createTime", user.getCreateTime());
        vo.put("updateTime", user.getUpdateTime());
        return vo;
    }

    /**
     * 将用户列表转换为VO列表
     * 
     * @param users 用户实体列表
     * @return 用户VO列表
     */
    public List<Map<String, Object>> buildUserVoList(List<User> users) {
        return users.stream()
                .map(this::buildUserVo)
                .toList();
    }
}
