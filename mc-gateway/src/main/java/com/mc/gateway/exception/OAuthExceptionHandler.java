package com.mc.gateway.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mc.common.entity.response.ResponseResult;
import com.mc.common.enums.Http;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * @author Xu huaiang
 * @date 2024/02/18
 */
@Component
public class OAuthExceptionHandler {

    public Mono<Void> writeError(ServerWebExchange exchange, String msg) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        ObjectMapper objectMapper = new ObjectMapper();
        ResponseResult responseResult = new ResponseResult();

        responseResult = ResponseResult.error(Http.LOGIN_EXPIRED.getCode(), msg);
        String resultInfoJson = null;
        DataBuffer buffer = null;
        try {
            //将响应对象转换为json字符串
            resultInfoJson = objectMapper.writeValueAsString(responseResult);
            buffer = response.bufferFactory().wrap(resultInfoJson.getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }
        return response.writeWith(Mono.just(buffer));
    }
}
