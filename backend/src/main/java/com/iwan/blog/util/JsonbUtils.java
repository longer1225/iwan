
package com.iwan.blog.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * JSONB字段操作工具类
 * 
 * <p>该工具类封装了对PostgreSQL JSONB字段的常用操作，包括：
 * <ul>
 *   <li>JSON字符串与Map对象的相互转换</li>
 *   <li>从Map中安全获取各种类型的值</li>
 *   <li>构建文章、评论等实体的JSONB文档</li>
 * </ul>
 * 
 * <p>设计目的：
 * <ul>
 *   <li>解耦Service层与JSONB操作细节</li>
 *   <li>提供统一的JSON处理入口</li>
 *   <li>增强代码可读性和可维护性</li>
 * </ul>
 */
@Component
public class JsonbUtils {

    private final ObjectMapper objectMapper;

    public JsonbUtils(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 将JSON字符串转换为Map对象
     * 
     * @param json JSON字符串
     * @return Map对象，若转换失败返回空Map
     */
    public Map<String, Object> parseJson(String json) {
        if (json == null || json.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            // 记录日志（实际项目中应使用日志框架）
            // log.warn("JSON解析失败: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * 将Map对象转换为JSON字符串
     * 
     * @param map Map对象
     * @return JSON字符串，若转换失败返回"{}"
     */
    public String toJson(Map<String, Object> map) {
        if (map == null) {
            return "{}";
        }
        try {
            return objectMapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            // 记录日志
            // log.warn("JSON序列化失败: {}", e.getMessage());
            return "{}";
        }
    }

    /**
     * 从Map中安全获取Long类型值
     * 
     * @param map 数据源
     * @param key 键名
     * @return Long值，若不存在或类型不匹配返回null
     */
    public Long getLongValue(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key)) {
            return null;
        }
        Object value = map.get(key);
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).longValue();
        } else if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        } else if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }

    /**
     * 从Map中安全获取Integer类型值
     * 
     * @param map 数据源
     * @param key 键名
     * @return Integer值，若不存在或类型不匹配返回null
     */
    public Integer getIntValue(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key)) {
            return null;
        }
        Object value = map.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Long) {
            return ((Long) value).intValue();
        } else if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return null;
    }

    /**
     * 从Map中安全获取String类型值
     * 
     * @param map 数据源
     * @param key 键名
     * @return String值，若不存在返回null
     */
    public String getStringValue(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key)) {
            return null;
        }
        Object value = map.get(key);
        return value != null ? String.valueOf(value) : null;
    }

    /**
     * 从Map中安全获取Boolean类型值
     * 
     * @param map 数据源
     * @param key 键名
     * @return Boolean值，若不存在返回null
     */
    public Boolean getBooleanValue(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key)) {
            return null;
        }
        Object value = map.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return Boolean.parseBoolean((String) value);
        }
        return null;
    }

    /**
     * 获取可见性设置，默认返回"PUBLIC"
     * 
     * @param map 数据源
     * @return 可见性字符串
     */
    public String getVisibility(Map<String, Object> map) {
        String visibility = getStringValue(map, "visibility");
        return visibility != null ? visibility : "PUBLIC";
    }

    /**
     * 构建文章文档Map（用于创建新文章）
     * 
     * @param title 文章标题
     * @param content 文章内容
     * @param categoryId 分类ID（支持String或Long）
     * @param tagList 标签列表
     * @param status 状态
     * @param cover 封面图
     * @param authorId 作者ID
     * @param anonymous 是否匿名
     * @param visibility 可见性
     * @param groupId 分组ID（支持String或Long）
     * @return 封装好的文档Map
     */
    public Map<String, Object> buildArticleDoc(
            String title, String content, Object categoryId, Object tagList,
            Object status, String cover, Long authorId, Boolean anonymous,
            String visibility, Object groupId) {
        
        Map<String, Object> doc = new HashMap<>();
        doc.put("title", title);
        doc.put("content", content);
        // 处理categoryId，支持String和Long类型
        doc.put("categoryId", convertToLong(categoryId));
        doc.put("tagList", tagList);
        doc.put("status", status);
        doc.put("cover", cover);
        doc.put("authorId", authorId);
        doc.put("readCount", 0);
        doc.put("likeCount", 0);
        doc.put("collectCount", 0);
        doc.put("commentCount", 0);
        
        // 处理匿名发布
        if (Boolean.TRUE.equals(anonymous)) {
            doc.put("authorName", "匿名用户");
            doc.put("authorAvatar", "");
            doc.put("anonymous", true);
        } else {
            doc.put("authorName", "");
            doc.put("authorAvatar", "");
            doc.put("anonymous", false);
        }
        
        // 设置可见权限
        doc.put("visibility", visibility != null ? visibility : "PUBLIC");
        // 处理groupId，支持String和Long类型
        doc.put("groupId", convertToLong(groupId));
        
        return doc;
    }
    
    /**
     * 将对象转换为Long类型
     * 
     * @param value 要转换的值
     * @return Long值，若转换失败返回null
     */
    private Long convertToLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).longValue();
        } else if (value instanceof String) {
            try {
                String strValue = ((String) value).trim();
                return strValue.isEmpty() ? null : Long.parseLong(strValue);
            } catch (NumberFormatException e) {
                return null;
            }
        } else if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return null;
    }

    /**
     * 构建评论文档Map
     * 
     * @param content 评论内容
     * @param authorId 评论者ID
     * @param articleId 文章ID
     * @param parentId 父评论ID（回复时使用）
     * @return 封装好的文档Map
     */
    public Map<String, Object> buildCommentDoc(String content, Long authorId, 
                                                Long articleId, Long parentId) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("content", content);
        doc.put("authorId", authorId);
        doc.put("articleId", articleId);
        doc.put("parentId", parentId);
        doc.put("likeCount", 0);
        return doc;
    }
}
