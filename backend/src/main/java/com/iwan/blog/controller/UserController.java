package com.iwan.blog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.iwan.blog.dto.ChangePasswordDTO;
import com.iwan.blog.dto.LoginDTO;
import com.iwan.blog.dto.RegisterDTO;
import com.iwan.blog.dto.UpdateUserInfoDTO;
import com.iwan.blog.entity.User;
import com.iwan.blog.service.UserService;
import com.iwan.blog.utils.JwtUtils;
import com.iwan.blog.vo.ResponseVO;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {

    private final UserService userService;
    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;

    public UserController(UserService userService, JwtUtils jwtUtils) {
        this.userService = userService;
        this.jwtUtils = jwtUtils;
        this.objectMapper = new ObjectMapper();
    }

    @PostMapping("/register")
    public ResponseVO<Map<String, Object>> register(@Valid @RequestBody RegisterDTO dto) {
        User user = userService.register(dto);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId().toString());
        String token = jwtUtils.generateToken(claims);

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("username", dto.getUsername());
        result.put("nickname", dto.getNickname());
        result.put("token", token);

        return ResponseVO.success(result);
    }

    @PostMapping("/login")
    public ResponseVO<Map<String, Object>> login(@Valid @RequestBody LoginDTO dto) {
        User user = userService.login(dto);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId().toString());
        String token = jwtUtils.generateToken(claims);

        // 从doc中提取用户信息
        Map<String, Object> doc = parseDoc(user.getDoc());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userId", user.getId());
        result.put("username", doc.get("username"));
        result.put("nickname", doc.getOrDefault("nickname", doc.get("username")));
        result.put("avatar", doc.getOrDefault("avatar", ""));
        result.put("role", "USER");

        return ResponseVO.success(result);
    }

    @PostMapping("/logout")
    public ResponseVO<Void> logout() {
        return ResponseVO.success();
    }

    @GetMapping("/info")
    public ResponseVO<Map<String, Object>> getInfo() {
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userService.getById(Long.parseLong(userId));

        // 从doc中提取用户信息
        Map<String, Object> doc = parseDoc(user.getDoc());

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("username", doc.get("username"));
        result.put("nickname", doc.getOrDefault("nickname", doc.get("username")));
        result.put("avatar", doc.getOrDefault("avatar", ""));
        result.put("bio", doc.getOrDefault("bio", ""));

        return ResponseVO.success(result);
    }

    @PutMapping("/info")
    public ResponseVO<Void> updateInfo(@RequestBody UpdateUserInfoDTO dto) {
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userService.updateUserInfo(Long.parseLong(userId), dto);
        return ResponseVO.success();
    }

    @PutMapping("/password")
    public ResponseVO<Void> changePassword(@Valid @RequestBody ChangePasswordDTO dto) {
        String userId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        userService.changePassword(Long.parseLong(userId), dto.getOldPassword(), dto.getNewPassword());
        return ResponseVO.success();
    }

    @GetMapping("/detail/{id}")
    public ResponseVO<Map<String, Object>> getDetail(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return ResponseVO.notFound("用户不存在");
        }

        // 从doc中提取用户信息
        Map<String, Object> doc = parseDoc(user.getDoc());

        Map<String, Object> result = new HashMap<>();
        result.put("userId", user.getId());
        result.put("username", doc.get("username"));
        result.put("nickname", doc.getOrDefault("nickname", doc.get("username")));
        result.put("avatar", doc.getOrDefault("avatar", ""));
        result.put("bio", doc.getOrDefault("bio", ""));

        return ResponseVO.success(result);
    }

    // 辅助方法：解析doc JSON字符串为Map
    private Map<String, Object> parseDoc(String docStr) {
        if (docStr == null || docStr.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(docStr, Map.class);
        } catch (Exception e) {
            return new HashMap<>();
        }
    }
}
