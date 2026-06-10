package com.iwan.blog.exception;

import com.iwan.blog.vo.ResponseVO;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理所有异常，返回标准化的错误响应
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ResponseVO<Void>> handleBusinessException(
            BusinessException ex, HttpServletRequest request) {
        
        // 记录日志
        logError("业务异常", ex.getCode(), ex.getMessage(), request);
        
        return ResponseEntity.ok(ResponseVO.error(ex.getCode(), ex.getMessage()));
    }

    /**
     * 处理参数校验异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ResponseVO<Void>> handleValidationException(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining("; "));
        
        String message = ExceptionCode.PARAMETER_INVALID.getMessage() + ": " + errors;
        
        // 记录日志
        logError("参数校验失败", ExceptionCode.PARAMETER_INVALID.getCode(), message, request);
        
        return ResponseEntity.ok(ResponseVO.error(ExceptionCode.PARAMETER_INVALID.getCode(), message));
    }

    /**
     * 处理参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ResponseVO<Void>> handleTypeMismatchException(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        
        String message = String.format("参数 '%s' 类型错误，期望类型: %s", 
                ex.getName(), 
                ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        
        // 记录日志
        logError("参数类型错误", ExceptionCode.PARAMETER_INVALID.getCode(), message, request);
        
        return ResponseEntity.ok(ResponseVO.error(ExceptionCode.PARAMETER_INVALID.getCode(), message));
    }

    /**
     * 处理非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseVO<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex, HttpServletRequest request) {
        
        // 记录日志
        logError("非法参数", ExceptionCode.PARAMETER_INVALID.getCode(), ex.getMessage(), request);
        
        return ResponseEntity.ok(ResponseVO.error(ExceptionCode.PARAMETER_INVALID.getCode(), ex.getMessage()));
    }

    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseVO<Void>> handleRuntimeException(
            RuntimeException ex, HttpServletRequest request) {
        
        // 记录日志（包含堆栈）
        logErrorWithStack("运行时异常", ExceptionCode.SYSTEM_ERROR.getCode(), ex.getMessage(), ex, request);
        
        return ResponseEntity.ok(ResponseVO.error(ExceptionCode.SYSTEM_ERROR.getCode(), 
                ExceptionCode.SYSTEM_ERROR.getMessage()));
    }

    /**
     * 处理所有其他异常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseVO<Void>> handleException(
            Exception ex, HttpServletRequest request) {
        
        // 记录日志（包含堆栈）
        logErrorWithStack("未知异常", ExceptionCode.INTERNAL_SERVER_ERROR.getCode(), ex.getMessage(), ex, request);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseVO.error(ExceptionCode.INTERNAL_SERVER_ERROR.getCode(), 
                        ExceptionCode.INTERNAL_SERVER_ERROR.getMessage()));
    }

    /**
     * 记录错误日志
     */
    private void logError(String type, int code, String message, HttpServletRequest request) {
        logger.error("[{}] [错误码: {}] [请求路径: {}] [请求方法: {}] [错误信息: {}]",
                type,
                code,
                request.getRequestURI(),
                request.getMethod(),
                message);
    }

    /**
     * 记录包含堆栈的错误日志
     */
    private void logErrorWithStack(String type, int code, String message, Throwable ex, HttpServletRequest request) {
        logger.error("[{}] [错误码: {}] [请求路径: {}] [请求方法: {}] [时间: {}] [错误信息: {}]",
                type,
                code,
                request.getRequestURI(),
                request.getMethod(),
                LocalDateTime.now().format(FORMATTER),
                message,
                ex);
    }
}
