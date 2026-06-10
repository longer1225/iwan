package com.iwan.blog.util;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 日志工具类
 * 提供统一的日志记录方法，方便调试和追踪
 */
public final class LogUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    private LogUtils() {
        // 私有构造函数，防止实例化
    }

    /**
     * 生成请求追踪ID
     */
    public static String generateTraceId() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }

    /**
     * 记录请求开始日志
     */
    public static void logRequestStart(Logger logger, HttpServletRequest request) {
        String traceId = generateTraceId();
        request.setAttribute("traceId", traceId);
        
        logger.info("[请求开始] [追踪ID: {}] [请求路径: {}] [请求方法: {}] [客户端IP: {}] [时间: {}]",
                traceId,
                request.getRequestURI(),
                request.getMethod(),
                getClientIp(request),
                LocalDateTime.now().format(FORMATTER));
    }

    /**
     * 记录请求结束日志
     */
    public static void logRequestEnd(Logger logger, HttpServletRequest request, long duration) {
        String traceId = (String) request.getAttribute("traceId");
        if (traceId == null) {
            traceId = "N/A";
        }
        
        logger.info("[请求结束] [追踪ID: {}] [请求路径: {}] [请求方法: {}] [耗时: {}ms] [时间: {}]",
                traceId,
                request.getRequestURI(),
                request.getMethod(),
                duration,
                LocalDateTime.now().format(FORMATTER));
    }

    /**
     * 记录业务操作日志
     */
    public static void logOperation(Logger logger, String operation, String detail) {
        logger.info("[业务操作] [操作类型: {}] [操作详情: {}] [时间: {}]",
                operation,
                detail,
                LocalDateTime.now().format(FORMATTER));
    }

    /**
     * 记录数据库操作日志
     */
    public static void logDatabaseOperation(Logger logger, String operation, String tableName, Object id) {
        logger.debug("[数据库操作] [操作类型: {}] [表名: {}] [记录ID: {}] [时间: {}]",
                operation,
                tableName,
                id != null ? id.toString() : "N/A",
                LocalDateTime.now().format(FORMATTER));
    }

    /**
     * 记录参数校验日志
     */
    public static void logValidation(Logger logger, String paramName, Object value) {
        logger.debug("[参数校验] [参数名: {}] [参数值: {}]",
                paramName,
                value != null ? value.toString() : "null");
    }

    /**
     * 记录警告日志
     */
    public static void logWarning(Logger logger, String message, Object... params) {
        logger.warn("[警告] [信息: {}]", String.format(message, params));
    }

    /**
     * 记录错误日志（不含堆栈）
     */
    public static void logError(Logger logger, String errorType, String message) {
        logger.error("[错误] [类型: {}] [信息: {}] [时间: {}]",
                errorType,
                message,
                LocalDateTime.now().format(FORMATTER));
    }

    /**
     * 记录错误日志（含堆栈）
     */
    public static void logErrorWithStack(Logger logger, String errorType, String message, Throwable ex) {
        logger.error("[错误] [类型: {}] [信息: {}] [时间: {}]",
                errorType,
                message,
                LocalDateTime.now().format(FORMATTER),
                ex);
    }

    /**
     * 获取客户端IP地址
     */
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果是多个代理，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 记录登录成功日志
     */
    public static void logLoginSuccess(Logger logger, String username, HttpServletRequest request) {
        logger.info("[登录成功] [用户名: {}] [客户端IP: {}] [时间: {}]",
                username,
                getClientIp(request),
                LocalDateTime.now().format(FORMATTER));
    }

    /**
     * 记录登录失败日志
     */
    public static void logLoginFailure(Logger logger, String username, String reason, HttpServletRequest request) {
        logger.warn("[登录失败] [用户名: {}] [失败原因: {}] [客户端IP: {}] [时间: {}]",
                username,
                reason,
                getClientIp(request),
                LocalDateTime.now().format(FORMATTER));
    }

    /**
     * 记录用户注册日志
     */
    public static void logUserRegister(Logger logger, String username, String email) {
        logger.info("[用户注册] [用户名: {}] [邮箱: {}] [时间: {}]",
                username,
                email,
                LocalDateTime.now().format(FORMATTER));
    }

    /**
     * 记录文件上传日志
     */
    public static void logFileUpload(Logger logger, String fileName, long fileSize, String savePath) {
        logger.info("[文件上传] [文件名: {}] [文件大小: {} bytes] [保存路径: {}] [时间: {}]",
                fileName,
                fileSize,
                savePath,
                LocalDateTime.now().format(FORMATTER));
    }
}
