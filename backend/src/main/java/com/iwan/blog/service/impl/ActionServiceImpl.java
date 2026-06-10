package com.iwan.blog.service.impl;

import com.iwan.blog.entity.Action;
import com.iwan.blog.mapper.ActionMapper;
import com.iwan.blog.service.ActionService;
import com.iwan.blog.vo.PageVO;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ActionServiceImpl implements ActionService {

    private final ActionMapper actionMapper;
    private final ObjectMapper objectMapper;

    public ActionServiceImpl(ActionMapper actionMapper, ObjectMapper objectMapper) {
        this.actionMapper = actionMapper;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public void toggleLike(Long userId, String targetId, String targetType) {
        LambdaQueryWrapper<Action> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Action::getIsDeleted, false)
                .apply("doc->>'userId' = {0}", userId.toString())
                .apply("doc->>'targetId' = {0}", targetId)
                .apply("doc->>'actionType' = 'LIKE'");

        Action existing = actionMapper.selectOne(wrapper);

        if (existing != null) {
            existing.setIsDeleted(true);
            actionMapper.updateById(existing);
        } else {
            Action action = new Action();
            Map<String, Object> doc = new HashMap<>();
            doc.put("userId", userId.toString());
            doc.put("targetId", targetId);
            doc.put("targetType", targetType);
            doc.put("actionType", "LIKE");
            try {
                action.setDoc(objectMapper.writeValueAsString(doc));
                actionMapper.insert(action);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("创建点赞记录失败", e);
            }
        }
    }

    @Override
    @Transactional
    public void toggleCollect(Long userId, String targetId, String targetType) {
        LambdaQueryWrapper<Action> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Action::getIsDeleted, false)
                .apply("doc->>'userId' = {0}", userId.toString())
                .apply("doc->>'targetId' = {0}", targetId)
                .apply("doc->>'actionType' = 'COLLECT'");

        Action existing = actionMapper.selectOne(wrapper);

        if (existing != null) {
            existing.setIsDeleted(true);
            actionMapper.updateById(existing);
        } else {
            Action action = new Action();
            Map<String, Object> doc = new HashMap<>();
            doc.put("userId", userId.toString());
            doc.put("targetId", targetId);
            doc.put("targetType", targetType);
            doc.put("actionType", "COLLECT");
            try {
                action.setDoc(objectMapper.writeValueAsString(doc));
                actionMapper.insert(action);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("创建收藏记录失败", e);
            }
        }
    }

    @Override
    public PageVO<Map<String, Object>> getUserLikes(Long userId, Integer pageNum, Integer pageSize) {
        Page<Action> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Action> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Action::getIsDeleted, false)
                .apply("doc->>'userId' = {0}", userId.toString())
                .apply("doc->>'actionType' = 'LIKE'")
                .orderByDesc(Action::getCreateTime);

        IPage<Action> result = actionMapper.selectPage(page, wrapper);

        List<Map<String, Object>> records = result.getRecords().stream()
                .map(action -> {
                    try {
                        return objectMapper.readValue(action.getDoc(), new TypeReference<Map<String, Object>>() {});
                    } catch (JsonProcessingException e) {
                        return new HashMap<String, Object>();
                    }
                })
                .toList();

        return PageVO.of(result.getTotal(), result.getPages(), result.getCurrent(), result.getSize(), records);
    }

    @Override
    public PageVO<Map<String, Object>> getUserCollects(Long userId, Integer pageNum, Integer pageSize) {
        Page<Action> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Action> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Action::getIsDeleted, false)
                .apply("doc->>'userId' = {0}", userId.toString())
                .apply("doc->>'actionType' = 'COLLECT'")
                .orderByDesc(Action::getCreateTime);

        IPage<Action> result = actionMapper.selectPage(page, wrapper);

        List<Map<String, Object>> records = result.getRecords().stream()
                .map(action -> {
                    try {
                        return objectMapper.readValue(action.getDoc(), new TypeReference<Map<String, Object>>() {});
                    } catch (JsonProcessingException e) {
                        return new HashMap<String, Object>();
                    }
                })
                .toList();

        return PageVO.of(result.getTotal(), result.getPages(), result.getCurrent(), result.getSize(), records);
    }
}
