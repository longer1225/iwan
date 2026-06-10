package com.iwan.blog.service;

import com.iwan.blog.dto.LoginDTO;
import com.iwan.blog.dto.RegisterDTO;
import com.iwan.blog.dto.UpdateUserInfoDTO;
import com.iwan.blog.entity.User;

public interface UserService {

    User register(RegisterDTO dto);

    User login(LoginDTO dto);

    User getById(Long id);

    User getByUsername(String username);

    User update(Long id, User user);

    void updateUserInfo(Long id, UpdateUserInfoDTO dto);

    void changePassword(Long id, String oldPassword, String newPassword);

    void delete(Long id);
}
