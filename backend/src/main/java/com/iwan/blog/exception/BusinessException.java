package com.iwan.blog.exception;

/**
 * 业务异常类
 */
public class BusinessException extends RuntimeException {

    private final int code;
    private final String message;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(ExceptionCode code) {
        super(code.getMessage());
        this.code = code.getCode();
        this.message = code.getMessage();
    }

    public BusinessException(ExceptionCode code, String detail) {
        super(code.getMessage() + ": " + detail);
        this.code = code.getCode();
        this.message = code.getMessage() + ": " + detail;
    }

    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
