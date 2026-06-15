
package com.iwan.blog.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.iwan.blog.dto.CommentDTO;
import com.iwan.blog.entity.Article;
import com.iwan.blog.entity.Comment;
import com.iwan.blog.mapper.ArticleMapper;
import com.iwan.blog.mapper.CommentMapper;
import com.iwan.blog.service.CommentService;
import com.iwan.blog.util.JsonbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 评论服务实现类
 * 
 * <p>该类实现了CommentService接口，提供评论的CRUD操作。
 * 通过依赖注入JsonbUtils工具类，解耦了JSONB字段的操作逻辑。
 * 
 * <p>核心功能：
 * <ul>
 *   <li>评论创建：使用JsonbUtils构建JSONB文档</li>
 *   <li>评论点赞：使用JsonbUtils解析和更新点赞状态</li>
 *   <li>评论列表：支持分页查询和回复查询</li>
 *   <li>文章评论计数更新：联动更新文章的评论数</li>
 * </ul>
 */
@Service
public class CommentServiceImpl implements CommentService {

    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(CommentServiceImpl.class);

    /**
     * 评论数据访问层
     */
    private final CommentMapper commentMapper;
    
    /**
     * JSONB操作工具类（解耦JSON处理逻辑）
     */
    private final JsonbUtils jsonbUtils;
    
    /**
     * 文章数据访问层（用于更新文章评论计数）
     */
    private final ArticleMapper articleMapper;

    /**
     * 构造函数依赖注入
     * 
     * @param commentMapper 评论Mapper
     * @param jsonbUtils JSONB工具类
     * @param articleMapper 文章Mapper
     */
    public CommentServiceImpl(CommentMapper commentMapper, JsonbUtils jsonbUtils, 
                             ArticleMapper articleMapper) {
        this.commentMapper = commentMapper;
        this.jsonbUtils = jsonbUtils;
        this.articleMapper = articleMapper;
    }

    /**
     * 创建评论
     * 
     * <p>使用JsonbUtils.buildCommentDoc()构建评论的JSONB文档，
     * 避免在Service层直接处理JSON序列化逻辑。
     * 创建成功后自动更新文章的评论计数。
     * 
     * @param dto 评论数据传输对象
     * @param userId 当前用户ID
     * @return 创建成功的评论实体
     */
    @Override
    @Transactional
    public Comment create(CommentDTO dto, Long userId) {
        Comment comment = new Comment();
        
        // 使用JsonbUtils构建评论文档
        Map<String, Object> doc = jsonbUtils.buildCommentDoc(
                dto.getContent(),
                userId,
                Long.parseLong(dto.getArticleId()),
                dto.getParentId() != null ? Long.parseLong(dto.getParentId()) : null
        );
        
        // 设置额外字段
        doc.put("userName", dto.getUserName() != null ? dto.getUserName() : "匿名用户");
        doc.put("userAvatar", dto.getUserAvatar() != null ? dto.getUserAvatar() : "");
        doc.put("anonymous", dto.getAnonymous());
        
        // 使用JsonbUtils序列化
        comment.setDoc(jsonbUtils.toJson(doc));

        // 插入数据库
        commentMapper.insert(comment);
        
        // 更新文章的评论计数
        updateArticleCommentCount(Long.parseLong(dto.getArticleId()), 1);
        
        return comment;
    }
    
    /**
     * 更新文章评论数
     * 
     * <p>私有方法，用于在评论创建/删除时联动更新文章的评论计数字段。
     * 使用JsonbUtils安全地解析和更新文章的JSONB文档。
     * 
     * @param articleId 文章ID
     * @param delta 增量（+1或-1）
     */
    private void updateArticleCommentCount(Long articleId, int delta) {
        Article article = articleMapper.selectById(articleId);
        if (article != null && article.getDoc() != null) {
            // 使用JsonbUtils解析文章文档
            Map<String, Object> doc = jsonbUtils.parseJson(article.getDoc());
            
            // 安全获取当前评论计数
            Integer currentCount = jsonbUtils.getIntValue(doc, "commentCount");
            if (currentCount == null) {
                currentCount = 0;
            }
            
            // 更新评论计数（确保不为负数）
            currentCount = Math.max(0, currentCount + delta);
            doc.put("commentCount", currentCount);
            
            // 使用JsonbUtils序列化回JSON
            article.setDoc(jsonbUtils.toJson(doc));
            articleMapper.updateById(article);
            
            logger.info("===== Updated commentCount for article {}: {}", articleId, currentCount);
        }
    }

    /**
     * 根据ID获取评论
     * 
     * @param id 评论ID
     * @return 评论实体
     */
    @Override
    public Comment getById(Long id) {
        return commentMapper.selectById(id);
    }

    /**
     * 删除评论
     * 
     * @param id 评论ID
     */
    @Override
    @Transactional
    public void delete(Long id) {
        commentMapper.deleteById(id);
    }

    /**
     * 分页查询文章的一级评论
     * 
     * <p>只查询parentId为空的评论（即顶级评论，非回复）。
     * 
     * @param page 分页对象
     * @param articleId 文章ID
     * @return 评论分页结果
     */
    @Override
    public IPage<Comment> list(Page<Comment> page, Long articleId) {
        return commentMapper.selectPage(page, 
            Wrappers.<Comment>lambdaQuery()
                .eq(Comment::getIsDeleted, false)
                .apply("doc->>'parentId' IS NULL OR doc->>'parentId' = ''")
                .apply("doc->>'articleId' = {0}", articleId.toString())
                .orderByDesc(Comment::getCreateTime));
    }
    
    /**
     * 评论点赞/取消点赞
     * 
     * <p>使用JsonbUtils解析评论文档，维护点赞用户列表和点赞计数。
     * 支持点赞和取消点赞两种操作。
     * 
     * @param commentId 评论ID
     * @param userId 当前用户ID
     * @return 更新后的评论实体
     * @throws RuntimeException 评论不存在时抛出异常
     */
    @Override
    @Transactional
    public Comment like(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getIsDeleted()) {
            throw new RuntimeException("评论不存在");
        }
        
        // 使用JsonbUtils解析评论文档
        Map<String, Object> doc = jsonbUtils.parseJson(comment.getDoc());
        
        // 获取当前点赞用户列表
        List<String> likedUserIds = new ArrayList<>();
        if (doc.containsKey("likedUserIds") && doc.get("likedUserIds") instanceof List) {
            List<?> tempList = (List<?>) doc.get("likedUserIds");
            for (Object obj : tempList) {
                likedUserIds.add(String.valueOf(obj));
            }
        }
        
        // 获取当前点赞计数
        Integer currentCount = jsonbUtils.getIntValue(doc, "likeCount");
        if (currentCount == null) {
            currentCount = 0;
        }
        
        String userIdStr = userId.toString();
        
        if (likedUserIds.contains(userIdStr)) {
            // 取消点赞：从列表中移除用户ID，计数减1
            likedUserIds.remove(userIdStr);
            currentCount = Math.max(0, currentCount - 1);
        } else {
            // 点赞：添加用户ID到列表，计数加1
            likedUserIds.add(userIdStr);
            currentCount++;
        }
        
        // 更新文档字段
        doc.put("likedUserIds", likedUserIds);
        doc.put("likeCount", currentCount);
        
        // 使用JsonbUtils序列化回JSON
        comment.setDoc(jsonbUtils.toJson(doc));
        commentMapper.updateById(comment);
        
        logger.info("===== Comment {} like count updated to {}", commentId, currentCount);
        return comment;
    }
    
    /**
     * 检查用户是否已点赞评论
     * 
     * @param commentId 评论ID
     * @param userId 用户ID
     * @return 是否已点赞
     */
    @Override
    public boolean isLiked(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null || comment.getIsDeleted()) {
            return false;
        }
        
        // 使用JsonbUtils解析评论文档
        Map<String, Object> doc = jsonbUtils.parseJson(comment.getDoc());
        
        // 检查用户ID是否在点赞列表中
        if (doc.containsKey("likedUserIds") && doc.get("likedUserIds") instanceof List) {
            List<?> likedUserIds = (List<?>) doc.get("likedUserIds");
            return likedUserIds.contains(userId.toString());
        }
        
        return false;
    }
    
    /**
     * 获取评论的回复列表
     * 
     * @param parentId 父评论ID
     * @return 回复评论列表
     */
    @Override
    public List<Comment> getReplies(Long parentId) {
        List<Comment> comments = commentMapper.selectList(
            Wrappers.<Comment>lambdaQuery()
                .eq(Comment::getIsDeleted, false)
                .apply("doc->>'parentId' IS NOT NULL AND doc->>'parentId' != ''")
                .apply("CAST(doc->>'parentId' AS BIGINT) = {0}", parentId)
                .orderByAsc(Comment::getCreateTime)
        );
        return comments != null ? comments : new ArrayList<>();
    }
}
