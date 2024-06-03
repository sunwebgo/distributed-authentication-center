package com.mc.common.exception;

import com.mc.common.entity.response.ResponseResult;
import com.mc.common.enums.Http;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 全局异常处理类
 * @author Xu huaiang
 * @date 2024/03/15
 */
@Slf4j
@RestControllerAdvice(basePackages = "com.mc")
public class GlobalExceptionHandler {
    /**
     * 数据校验异常处理
     *
     * @param e e
     * @return {@link ResponseResult}
     */
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseResult handleValidException(MethodArgumentNotValidException e) {
        log.error("数据校验出现问题：" + e.getMessage() + "异常类型是：" + e.getClass());
//        得到数据校验的错误结果
        BindingResult bindingResult = e.getBindingResult();
        Map<String, String> errorMap = new HashMap<>();
        bindingResult.getFieldErrors().forEach((fieldError -> {
            errorMap.put(fieldError.getField(), fieldError.getDefaultMessage());
        }));
        String message = errorMap.values().stream().collect(Collectors.joining(", "));
        return ResponseResult.error(message);
    }

    /**
     * 全局异常处理
     *
     * @return {@link ResponseResult}
     */
    @ExceptionHandler(value = Throwable.class)
    public ResponseResult handlerException(Exception e) {
        log.error("异常类型是：" + e.getClass() + "异常信息是：" + e.getMessage());
        return ResponseResult.error(e.getMessage().split(":")[2]);
    }
}
