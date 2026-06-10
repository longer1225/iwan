package com.iwan.blog.vo;

import lombok.Data;

@Data
public class ResponseVO<T> {

    private Integer code;
    private String msg;
    private T data;
    private Object extend;

    private ResponseVO(Integer code, String msg, T data, Object extend) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.extend = extend;
    }

    public static <T> ResponseVO<T> success() {
        return new ResponseVO<>(200, "请求成功", null, null);
    }

    public static <T> ResponseVO<T> success(T data) {
        return new ResponseVO<>(200, "请求成功", data, null);
    }

    public static <T> ResponseVO<T> success(T data, Object extend) {
        return new ResponseVO<>(200, "请求成功", data, extend);
    }

    public static <T> ResponseVO<T> error(Integer code, String msg) {
        return new ResponseVO<>(code, msg, null, null);
    }

    public static <T> ResponseVO<T> error(String msg) {
        return new ResponseVO<>(500, msg, null, null);
    }

    public static <T> ResponseVO<T> badRequest(String msg) {
        return new ResponseVO<>(400, msg, null, null);
    }

    public static <T> ResponseVO<T> unauthorized() {
        return new ResponseVO<>(401, "未登录或Token失效", null, null);
    }

    public static <T> ResponseVO<T> forbidden(String msg) {
        return new ResponseVO<>(403, msg, null, null);
    }

    public static <T> ResponseVO<T> notFound(String msg) {
        return new ResponseVO<>(404, msg, null, null);
    }

    public static <T> ResponseVO<T> conflict(String msg) {
        return new ResponseVO<>(409, msg, null, null);
    }
}
