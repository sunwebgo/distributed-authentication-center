package com.mc.common.enums;

public enum Http {
    SUCCESS(200, "成功"),
    LOGIN_SUCCESS(200, "登录成功"),

    NEED_LOGIN(500, "请登录"),
    NOT_PERMISSION(500, "您没有权限"),
    LOGIN_FAIL(500, "登录失败"),
    LOGIN_EXPIRED(500, "登录已过期，请重新登录");


    final int code;
    final String message;

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
