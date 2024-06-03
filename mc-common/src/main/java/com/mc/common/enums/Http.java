package com.mc.common.enums;

public enum Http {
    SUCCESS(200, "成功"),
    LOGIN_SUCCESS(200, "登录成功"),
    CAPTCHA_SEND_SUCCESS(200, "验证码发送成功"),
    REVOKE_SUCCESS(200, "注销成功"),
    REGISTER_SUCCESS(200, "注册成功"),
    UPDATE_SUCCESS(200, "修改成功"),
    LIKE_SUCCESS(200, "点赞成功"),
    LIKE_CANCEL(200, "取消点赞"),
    ATTENTION_SUCCESS(200, "关注成功"),
    CANCEL_ATTENTION_SUCCESS(200, "取消关注成功"),
    CANCEL_LIKE_SUCCESS(200, "取消点赞成功"),

    NEED_LOGIN(500, "请登录"),
    NOT_PERMISSION(500, "您没有权限"),
    LOGIN_FAIL(500, "登录失败，请重新登录"),
    LOGIN_EXPIRED(500, "登录已过期，请重新登录"),
    PHONE_REFUSE(500, "手机号码不合法"),
    PHONE_NOT_NULL(500, "手机号不能为空"),
    CAPTCHA_SEND_FAIL(500, "验证码发送失败"),
    CAPTCHA_ERROR(500, "验证码错误"),
    CAPTCHA_REFUSE(500, "验证码不合法"),
    USERNAME_OR_PASSWORD_ERROR(500, "用户名或密码错误"),
    DELETE_DYNAMIC_FAIL(500, "删除动态失败"),
    CAPTCHA_EXPIRED(500, "验证码已过期"),
    DYNAMIC_LIST_FAIL(500, "查询个人动态列表失败"),
    CAPTCHA_HOUR_LIMIT(500, "此手机号当前小时内验证码发送次数超过限制"),
    CAPTCHA_DAY_LIMIT(500, "此手机号当天验证码发送次数超过限制"),
    INFO_ERROR(500, "信息输入有误"),
    MUSIC_INFO_INIT_FAIL(500, "音乐数据初始化失败"),
    USER_AVATAR_INFO_ERROR(500, "用户头像信息有误"),
    USERNAME_REFUSE(500, "用户名不合法"),
    DYNAMIC_ID_NOT_NULL(500, "动态ID不能为空"),
    PUBLISH_DYNAMIC_FAIL(500, "发布动态失败"),
    USERNAME_NOT_NULL(500, "用户名不能为空"),
    USER_INFO_NOT_NULL(500, "用户信息不能为空"),
    UPDATE_FAIL(500, "修改失败"),
    PARENT_COMMENT_NOT_EXIST(500, "父评论不存在"),
    COMMENT_NOT_EXIST(500,"评论不存在"),
    COMMENT_NOT_BELONG(500,"当前评论不属于该用户"),
    MUSIC_GET_FAIL(500, "获取音乐失败"),
    MUSIC_NOT_EXIST(500, "音乐不存在"),
    USER_NOT_EXIST(500, "用户不存在"),
    ATTENTION_USER_NOT_EXIST(500, "关注的用户不存在"),
    MUSIC_NOT_NULL(500, "请选择音乐"),
    DYNAMIC_NOT_NULL(500, "请选择动态"),
    DYNAMIC_NOT_EXIST(500, "动态不存在"),
    USER_INFO_GET_FAIL(500, "用户信息获取失败"),
    REVOKE_FAIL(500, "注销失败"),
    PASSWORD_ERROR(500, "密码错误"),
    INSERT_ROLE_FAIL(500, "为用户赋予角色失败"),
    SEARCH_KEYWORD_NOT_NULL(500, "搜索关键字不能为空"),
    USER_EXIST(500, "用户名或手机号已注册"),
    ATTENTION_USER_NOT_EXIST_PART(500, "关注的部分用户不存在"),
    PASSWORD_NOT_SAME(500, "两次密码不一致"),
    PASSWORD_NOT_NULL(500, "密码不能为空"),
    OLD_PASSWORD_ERROR(500, "旧密码错误"),
    USER_OR_ATTENTION_NOT_EXIST(500, "当前用户或者被关注用户不存在"),
    USER_NOT_ATTENTION_SELF(500, "用户不能关注自己"),
    PASSWORD_FORMAT_ERROR(500, "密码格式不正确"),
    REGISTER_FAIL(500, "注册失败"),
    USERNAME_EXIST(500, "用户名已注册"),
    COMMENT_FAIL(500, "评论失败"),
    PHONE_EXIST(500, "手机号已注册"),
    USER_INFO_FAIL(500, "用户信息获取失败"),
    GET_USER_ROLE_FAIL(500, "用户角色信息获取失败"),
    COLLECT_FAIL(500, "点赞失败"),
    ROLE_PERMISSION_INIT_FAIL(500, "角色权限初始化失败"),
    DYNAMIC_INFO_INIT_FAIL(500, "动态数据初始化失败"),
    CANCEL_LIKE_FAIL(500, "取消点赞失败"),
    PLEASE_USERNAME(500, "请输入用户名"),
    PLEASE_PASSWORD(500, "请输入密码"),
    SYSTEM_ERROR(500, "系统错误");


    private final int code;
    private final String message;

    Http(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
