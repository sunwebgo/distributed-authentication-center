package com.mc.common.entity.response;


import java.io.Serializable;

/**
 * @author Xu huaiang
 * @description 统一响应结果
 * @date 2024/02/05
 */
public class ResponseResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Integer code;
    private String message;
    private T data;

    public ResponseResult() {
    }

    public ResponseResult(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public ResponseResult(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static ResponseResult success() {
        return new ResponseResult(200, "success");
    }

    public static ResponseResult success(String message) {
        return new ResponseResult(200, message);
    }

    public static ResponseResult success(Object data) {
        return new ResponseResult(200, "success", data);
    }

    public static ResponseResult success(String message, Object data) {
        return new ResponseResult(200, message, data);
    }

    public static ResponseResult error() {
        return new ResponseResult(500, "error");
    }

    public static ResponseResult error(String message) {
        return new ResponseResult(500, message);
    }

    public static ResponseResult error(Integer code, String message) {
        return new ResponseResult(code, message);
    }

    public static ResponseResult error(Integer code, String message, Object data) {
        return new ResponseResult(code, message, data);
    }

    public Integer getCode() {
        return code;
    }

    public ResponseResult setCode(Integer code) {
        this.code = code;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public ResponseResult setMessage(String message) {
        this.message = message;
        return this;
    }

    public T getData() {
        return data;
    }

    public ResponseResult setData(T data) {
        this.data = data;
        return this;
    }
}
