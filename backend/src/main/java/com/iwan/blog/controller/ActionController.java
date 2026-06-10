package com.iwan.blog.controller;

import com.iwan.blog.service.ActionService;
import com.iwan.blog.vo.PageVO;
import com.iwan.blog.vo.ResponseVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/action")
public class ActionController {

    private static final Logger logger = LoggerFactory.getLogger(ActionController.class);

    private final ActionService actionService;

    public ActionController(ActionService actionService) {
        this.actionService = actionService;
    }

    @PostMapping("/like")
    public ResponseVO<Map<String, Object>> like(@RequestBody Map<String, String> body) {
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        logger.info("Like action - userId: {}, targetId: {}, targetType: {}", userId, body.get("targetId"), body.get("targetType"));
        String targetId = body.get("targetId");
        String targetType = body.get("targetType");

        actionService.toggleLike(Long.parseLong(userId), targetId, targetType);
        return ResponseVO.success(Map.of("liked", true));
    }

    @PostMapping("/collect")
    public ResponseVO<Map<String, Object>> collect(@RequestBody Map<String, String> body) {
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        logger.info("Collect action - userId: {}, targetId: {}, targetType: {}", userId, body.get("targetId"), body.get("targetType"));
        String targetId = body.get("targetId");
        String targetType = body.get("targetType");

        actionService.toggleCollect(Long.parseLong(userId), targetId, targetType);
        return ResponseVO.success(Map.of("collected", true));
    }

    @GetMapping("/likes")
    public ResponseVO<PageVO<Map<String, Object>>> getLikes(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        logger.info("Get likes - userId: {}, pageNum: {}, pageSize: {}", userId, pageNum, pageSize);
        PageVO<Map<String, Object>> result = actionService.getUserLikes(Long.parseLong(userId), pageNum, pageSize);
        logger.info("Get likes result - total: {}, records size: {}", result.getTotal(), result.getRecords() != null ? result.getRecords().size() : 0);
        return ResponseVO.success(result);
    }

    @GetMapping("/likes/check")
    public ResponseVO<Boolean> checkLike(
            @RequestParam String targetId,
            @RequestParam String targetType) {
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean liked = actionService.isLiked(Long.parseLong(userId), targetId, targetType);
        return ResponseVO.success(liked);
    }

    @GetMapping("/collects")
    public ResponseVO<PageVO<Map<String, Object>>> getCollects(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        PageVO<Map<String, Object>> result = actionService.getUserCollects(Long.parseLong(userId), pageNum, pageSize);
        return ResponseVO.success(result);
    }

    @GetMapping("/collects/check")
    public ResponseVO<Boolean> checkCollect(
            @RequestParam String targetId,
            @RequestParam String targetType) {
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        boolean collected = actionService.isCollected(Long.parseLong(userId), targetId, targetType);
        return ResponseVO.success(collected);
    }
}
