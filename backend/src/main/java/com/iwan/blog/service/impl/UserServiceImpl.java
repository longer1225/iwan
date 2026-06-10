package com.iwan.blog.service.impl;

import com.iwan.blog.dto.LoginDTO;
import com.iwan.blog.dto.RegisterDTO;
import com.iwan.blog.dto.UpdateUserInfoDTO;
import com.iwan.blog.entity.User;
import com.iwan.blog.exception.BusinessException;
import com.iwan.blog.exception.ExceptionCode;
import com.iwan.blog.mapper.UserMapper;
import com.iwan.blog.service.UserService;
import com.iwan.blog.utils.PasswordUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserMapper userMapper;
    private final PasswordUtils passwordUtils;
    private final ObjectMapper objectMapper;

    public UserServiceImpl(UserMapper userMapper, PasswordUtils passwordUtils, ObjectMapper objectMapper) {
        this.userMapper = userMapper;
        this.passwordUtils = passwordUtils;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public User register(RegisterDTO dto) {
        logger.info("用户注册: username={}, email={}", dto.getUsername(), dto.getEmail());
        
        if (getByUsername(dto.getUsername()) != null) {
            logger.warn("用户注册失败: 账号已存在, username={}", dto.getUsername());
            throw new BusinessException(ExceptionCode.USER_ALREADY_EXISTS);
        }

        User user = new User();
        Map<String, Object> doc = new HashMap<>();
        doc.put("username", dto.getUsername());
        doc.put("password", passwordUtils.encode(dto.getPassword()));
        doc.put("nickname", dto.getNickname());
        doc.put("email", dto.getEmail());
        doc.put("role", "USER");
        doc.put("avatar", "");
        doc.put("bio", "");

        try {
            user.setDoc(objectMapper.writeValueAsString(doc));
        } catch (JsonProcessingException e) {
            logger.error("用户注册失败: 序列化异常, username={}", dto.getUsername(), e);
            throw new BusinessException(ExceptionCode.DATABASE_ERROR, "序列化失败");
        }

        userMapper.insert(user);
        logger.info("用户注册成功: userId={}, username={}", user.getId(), dto.getUsername());
        return user;
    }

    @Override
    public User login(LoginDTO dto) {
        logger.info("用户登录: username={}", dto.getUsername());
        
        User user = getByUsername(dto.getUsername());
        if (user == null) {
            logger.warn("用户登录失败: 账号不存在, username={}", dto.getUsername());
            throw new BusinessException(ExceptionCode.USER_NOT_FOUND);
        }

        try {
            Map<String, Object> doc = objectMapper.readValue(user.getDoc(), Map.class);
            String encodedPassword = (String) doc.get("password");
            if (!passwordUtils.matches(dto.getPassword(), encodedPassword)) {
                logger.warn("用户登录失败: 密码错误, username={}", dto.getUsername());
                throw new BusinessException(ExceptionCode.USERNAME_OR_PASSWORD_ERROR);
            }
        } catch (JsonProcessingException e) {
            logger.error("用户登录失败: 解析异常, username={}", dto.getUsername(), e);
            throw new BusinessException(ExceptionCode.DATABASE_ERROR, "解析失败");
        }

        logger.info("用户登录成功: userId={}, username={}", user.getId(), dto.getUsername());
        return user;
    }

    @Override
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public User getByUsername(String username) {
        return userMapper.findByUsername(username);
    }

    @Override
    @Transactional
    public User update(Long id, User user) {
        user.setId(id);
        userMapper.updateById(user);
        return user;
    }

    @Override
    @Transactional
    public void updateUserInfo(Long id, UpdateUserInfoDTO dto) {
        logger.info("更新用户信息: userId={}", id);
        
        User user = userMapper.selectById(id);
        if (user == null) {
            logger.warn("更新用户信息失败: 用户不存在, userId={}", id);
            throw new BusinessException(ExceptionCode.USER_NOT_FOUND);
        }

        try {
            Map<String, Object> doc = objectMapper.readValue(user.getDoc(), Map.class);
            
            // 更新需要修改的字段
            if (dto.getNickname() != null && !dto.getNickname().isEmpty()) {
                doc.put("nickname", dto.getNickname());
            }
            if (dto.getAvatar() != null) {
                doc.put("avatar", dto.getAvatar());
            }
            if (dto.getBio() != null) {
                doc.put("bio", dto.getBio());
            }

            user.setDoc(objectMapper.writeValueAsString(doc));
            userMapper.updateById(user);
            logger.info("更新用户信息成功: userId={}", id);
        } catch (JsonProcessingException e) {
            logger.error("更新用户信息失败: 序列化异常, userId={}", id, e);
            throw new BusinessException(ExceptionCode.DATABASE_ERROR, "更新用户信息失败");
        }
    }

    @Override
    @Transactional
    public void changePassword(Long id, String oldPassword, String newPassword) {
        logger.info("修改密码: userId={}", id);
        
        User user = userMapper.selectById(id);
        if (user == null) {
            logger.warn("修改密码失败: 用户不存在, userId={}", id);
            throw new BusinessException(ExceptionCode.USER_NOT_FOUND);
        }

        try {
            Map<String, Object> doc = objectMapper.readValue(user.getDoc(), Map.class);
            String encodedPassword = (String) doc.get("password");
            
            // 验证旧密码
            if (!passwordUtils.matches(oldPassword, encodedPassword)) {
                logger.warn("修改密码失败: 旧密码错误, userId={}", id);
                throw new BusinessException(ExceptionCode.OLD_PASSWORD_ERROR);
            }

            // 更新新密码
            doc.put("password", passwordUtils.encode(newPassword));
            user.setDoc(objectMapper.writeValueAsString(doc));
            userMapper.updateById(user);
            logger.info("修改密码成功: userId={}", id);
        } catch (JsonProcessingException e) {
            logger.error("修改密码失败: 序列化异常, userId={}", id, e);
            throw new BusinessException(ExceptionCode.DATABASE_ERROR, "修改密码失败");
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        userMapper.deleteById(id);
    }
}
