package com.iwan.blog.service;

import com.iwan.blog.vo.PageVO;

import java.util.Map;

public interface ActionService {

    void toggleLike(Long userId, String targetId, String targetType);

    void toggleCollect(Long userId, String targetId, String targetType);

    PageVO<Map<String, Object>> getUserLikes(Long userId, Integer pageNum, Integer pageSize);

    PageVO<Map<String, Object>> getUserCollects(Long userId, Integer pageNum, Integer pageSize);

    boolean isLiked(Long userId, String targetId, String targetType);

    boolean isCollected(Long userId, String targetId, String targetType);
}
