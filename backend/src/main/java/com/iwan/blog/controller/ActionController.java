// 接口控制器包
package com.iwan.blog.controller;

// 注入业务服务
import com.iwan.blog.service.ActionService;
// 分页返回实体、统一接口返回体
import com.iwan.blog.vo.PageVO;
import com.iwan.blog.vo.ResponseVO;
// 日志
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// SpringSecurity 上下文：获取当前登录用户
import org.springframework.security.core.context.SecurityContextHolder;
// Web注解
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 行为相关接口控制器
 * 接口前缀：/api/v1/action
 * 功能：点赞、取消点赞、收藏、取消收藏、查询我的点赞/收藏、校验点赞/收藏状态
 */
@RestController // 标识为接口控制器，返回JSON数据
@RequestMapping("/api/v1/action") // 统一接口路径前缀
public class ActionController {

    // 日志对象
    private static final Logger logger = LoggerFactory.getLogger(ActionController.class);

    // 注入行为业务服务
    private final ActionService actionService;

    // 构造器注入Service
    public ActionController(ActionService actionService) {
        this.actionService = actionService;
    }

    /**
     * 点赞/取消点赞 接口
     * 请求方式：POST
     * 地址：/api/v1/action/like
     * @param body 前端传参：{targetId: "文章ID", targetType: "目标类型"}
     * @return 统一响应结果
     */
    @PostMapping("/like")
    public ResponseVO<Map<String, Object>> like(@RequestBody Map<String, String> body) {
        // 从SpringSecurity上下文获取当前登录用户ID（登录认证后存入）
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // 打印请求日志
        logger.info("Like action - userId: {}, targetId: {}, targetType: {}", userId, body.get("targetId"), body.get("targetType"));
        
        // 从请求体取出参数
        String targetId = body.get("targetId");
        String targetType = body.get("targetType");

        // 调用Service层 点赞切换逻辑
        actionService.toggleLike(Long.parseLong(userId), targetId, targetType);
        // 向前端返回成功结果
        return ResponseVO.success(Map.of("liked", true));
    }

    /**
     * 收藏/取消收藏 接口
     * 请求方式：POST
     * 地址：/api/v1/action/collect
     */
    @PostMapping("/collect")
    public ResponseVO<Map<String, Object>> collect(@RequestBody Map<String, String> body) {
        // 获取当前登录用户ID
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        logger.info("Collect action - userId: {}, targetId: {}, targetType: {}", userId, body.get("targetId"), body.get("targetType"));
        
        String targetId = body.get("targetId");
        String targetType = body.get("targetType");

        // 调用Service收藏切换逻辑
        actionService.toggleCollect(Long.parseLong(userId), targetId, targetType);
        return ResponseVO.success(Map.of("collected", true));
    }

    /**
     * 分页查询 当前用户的点赞列表
     * 请求方式：GET
     * 地址：/api/v1/action/likes
     * @param pageNum 页码，默认1
     * @param pageSize 每页条数，默认10
     * @return 分页数据
     */
    @GetMapping("/likes")
    public ResponseVO<PageVO<Map<String, Object>>> getLikes(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        // 获取登录用户ID
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        logger.info("Get likes - userId: {}, pageNum: {}, pageSize: {}", userId, pageNum, pageSize);
        
        // 调用Service查询点赞列表
        PageVO<Map<String, Object>> result = actionService.getUserLikes(Long.parseLong(userId), pageNum, pageSize);
        logger.info("Get likes result - total: {}, records size: {}", result.getTotal(), result.getRecords() != null ? result.getRecords().size() : 0);
        return ResponseVO.success(result);
    }

    /**
     * 【重点】校验是否已点赞
     * 对应你之前 Service 里的 isLiked 方法
     * 请求地址：/api/v1/action/likes/check
     */
    @GetMapping("/likes/check")
    public ResponseVO<Boolean> checkLike(
            @RequestParam String targetId,    // 目标文章ID
            @RequestParam String targetType) { // 目标类型
        // 获取当前登录用户ID
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // 调用 Service.isLiked() 方法！！这就是你要找的调用位置
        boolean liked = actionService.isLiked(Long.parseLong(userId), targetId, targetType);
        // 返回布尔值给前端
        return ResponseVO.success(liked);
    }

    /**
     * 分页查询 当前用户的收藏列表
     * 请求地址：/api/v1/action/collects
     */
    @GetMapping("/collects")
    public ResponseVO<PageVO<Map<String, Object>>> getCollects(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        PageVO<Map<String, Object>> result = actionService.getUserCollects(Long.parseLong(userId), pageNum, pageSize);
        return ResponseVO.success(result);
    }

    /**
     * 【重点】校验是否已收藏
     * 对应 Service.isCollected 方法
     * 请求地址：/api/v1/action/collects/check
     */
    @GetMapping("/collects/check")
    public ResponseVO<Boolean> checkCollect(
            @RequestParam String targetId,
            @RequestParam String targetType) {
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // 调用 Service.isCollected()
        boolean collected = actionService.isCollected(Long.parseLong(userId), targetId, targetType);
        return ResponseVO.success(collected);
    }
}