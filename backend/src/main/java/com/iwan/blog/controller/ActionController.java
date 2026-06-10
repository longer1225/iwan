package com.iwan.blog.controller;

import com.iwan.blog.service.ActionService;
import com.iwan.blog.vo.PageVO;
import com.iwan.blog.vo.ResponseVO;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/action")
public class ActionController {

    private final ActionService actionService;

    public ActionController(ActionService actionService) {
        this.actionService = actionService;
    }

    @PostMapping("/like")
    public ResponseVO<Map<String, Object>> like(@RequestBody Map<String, String> body) {
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String targetId = body.get("targetId");
        String targetType = body.get("targetType");

        actionService.toggleLike(Long.parseLong(userId), targetId, targetType);
        return ResponseVO.success(Map.of("liked", true));
    }

    @PostMapping("/collect")
    public ResponseVO<Map<String, Object>> collect(@RequestBody Map<String, String> body) {
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
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
        PageVO<Map<String, Object>> result = actionService.getUserLikes(Long.parseLong(userId), pageNum, pageSize);
        return ResponseVO.success(result);
    }

    @GetMapping("/collects")
    public ResponseVO<PageVO<Map<String, Object>>> getCollects(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        PageVO<Map<String, Object>> result = actionService.getUserCollects(Long.parseLong(userId), pageNum, pageSize);
        return ResponseVO.success(result);
    }
}
