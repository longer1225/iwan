package com.iwan.blog.exception;

/**
 * 业务异常错误码枚举
 * 
 * 错误码规则：
 * - 10000-10099: 用户相关错误
 * - 10100-10199: 文章相关错误
 * - 10200-10299: 评论相关错误
 * - 10300-10399: 点赞/收藏相关错误
 * - 10400-10499: 好友/社交相关错误
 * - 10500-10599: 文件上传相关错误
 * - 10600-10699: AI相关错误
 * - 10700-10799: 系统/权限相关错误
 */
public enum ExceptionCode {

    // ==================== 用户相关错误 (10000-10099) ====================
    USER_NOT_FOUND(10000, "用户不存在"),
    USER_ALREADY_EXISTS(10001, "用户已存在"),
    USERNAME_OR_PASSWORD_ERROR(10002, "用户名或密码错误"),
    OLD_PASSWORD_ERROR(10003, "旧密码错误"),
    PASSWORD_NOT_MATCH(10004, "两次输入的密码不一致"),
    USER_NOT_LOGGED_IN(10005, "用户未登录"),
    USER_TOKEN_EXPIRED(10006, "登录已过期，请重新登录"),
    USER_TOKEN_INVALID(10007, "无效的登录凭证"),
    USER_PERMISSION_DENIED(10008, "用户权限不足"),
    USER_NICKNAME_EMPTY(10009, "昵称不能为空"),

    // ==================== 文章相关错误 (10100-10199) ====================
    ARTICLE_NOT_FOUND(10100, "文章不存在"),
    ARTICLE_TITLE_EMPTY(10101, "文章标题不能为空"),
    ARTICLE_CONTENT_EMPTY(10102, "文章内容不能为空"),
    ARTICLE_NOT_PUBLISHED(10103, "文章未发布"),
    ARTICLE_NOT_AUTHOR(10104, "无权限操作该文章"),
    ARTICLE_STATUS_INVALID(10105, "文章状态无效"),
    ARTICLE_CATEGORY_NOT_FOUND(10106, "文章分类不存在"),

    // ==================== 评论相关错误 (10200-10299) ====================
    COMMENT_NOT_FOUND(10200, "评论不存在"),
    COMMENT_CONTENT_EMPTY(10201, "评论内容不能为空"),
    COMMENT_NOT_AUTHOR(10202, "无权限操作该评论"),
    COMMENT_PARENT_NOT_FOUND(10203, "父评论不存在"),

    // ==================== 点赞/收藏相关错误 (10300-10399) ====================
    LIKE_TARGET_NOT_FOUND(10300, "点赞目标不存在"),
    COLLECT_TARGET_NOT_FOUND(10301, "收藏目标不存在"),
    LIKE_ALREADY_EXISTS(10302, "已点赞"),
    COLLECT_ALREADY_EXISTS(10303, "已收藏"),

    // ==================== 好友/社交相关错误 (10400-10499) ====================
    FRIEND_NOT_FOUND(10400, "好友不存在"),
    FRIEND_REQUEST_NOT_FOUND(10401, "好友请求不存在"),
    FRIEND_REQUEST_ALREADY_SENT(10402, "已发送好友请求"),
    CANNOT_ADD_YOURSELF(10403, "不能添加自己为好友"),
    ALREADY_FRIENDS(10404, "已经是好友"),
    FRIEND_REQUEST_EXPIRED(10405, "好友请求已过期"),

    // ==================== 文件上传相关错误 (10500-10599) ====================
    FILE_EMPTY(10500, "请选择要上传的文件"),
    FILE_TYPE_INVALID(10501, "文件类型无效"),
    FILE_SIZE_EXCEEDED(10502, "文件大小超过限制"),
    FILE_UPLOAD_FAILED(10503, "文件上传失败"),
    FILE_NOT_FOUND(10504, "文件不存在"),
    AVATAR_UPLOAD_FAILED(10505, "头像上传失败"),

    // ==================== AI相关错误 (10600-10699) ====================
    AI_SERVICE_UNAVAILABLE(10600, "AI服务暂不可用"),
    AI_REQUEST_TOO_FREQUENT(10601, "请求过于频繁，请稍后再试"),
    AI_PROMPT_EMPTY(10602, "请输入提示词"),
    AI_GENERATION_FAILED(10603, "内容生成失败"),
    AI_TOKEN_EXCEEDED(10604, "请求内容过长"),

    // ==================== 系统/权限相关错误 (10700-10799) ====================
    SYSTEM_ERROR(10700, "系统内部错误"),
    PARAMETER_INVALID(10701, "参数无效"),
    PARAMETER_MISSING(10702, "缺少必要参数"),
    REQUEST_METHOD_NOT_ALLOWED(10703, "不支持的请求方法"),
    ACCESS_DENIED(10704, "访问被拒绝"),
    RESOURCE_NOT_FOUND(10705, "资源不存在"),
    DATABASE_ERROR(10706, "数据库操作失败"),
    CACHE_ERROR(10707, "缓存操作失败"),
    MESSAGE_QUEUE_ERROR(10708, "消息队列操作失败"),

    // ==================== HTTP状态码对应 (20000+) ====================
    BAD_REQUEST(20000, "请求参数错误"),
    UNAUTHORIZED(20001, "未授权"),
    FORBIDDEN(20003, "禁止访问"),
    NOT_FOUND(20004, "资源未找到"),
    INTERNAL_SERVER_ERROR(20005, "服务器内部错误");

    private final int code;
    private final String message;

    ExceptionCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * 根据错误码获取异常枚举
     */
    public static ExceptionCode fromCode(int code) {
        for (ExceptionCode exceptionCode : values()) {
            if (exceptionCode.code == code) {
                return exceptionCode;
            }
        }
        return SYSTEM_ERROR;
    }
}
