package com.mc.gateway.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mc.common.constants.CacheConstants;
import com.mc.common.constants.CommonConstants;
import com.mc.common.constants.OAuthConstants;
import com.mc.common.dubbo.UserServiceInterface;
import com.mc.common.entity.response.ResponseResult;
import com.mc.common.enums.Http;
import com.mc.common.utils.RedisUtil;
import com.mc.gateway.entity.TokenCheckInfo;
import com.mc.gateway.exception.OAuthExceptionHandler;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author Xu huaiang
 * @description 刷新token切面，目的是在token过期，但是refresh_token未过期的情况下，
 * 通过refresh_token刷新token，刷新成功后，
 * 将refresh_token和新的token封装到请求响应体中。
 * @date 2024/03/04
 */
@Aspect
@Component
public class RefreshTokenAspect {
    @Resource
    private RestTemplate restTemplate;

    @Value("${security.oauth2.client.client-id}")
    private String clientId;

    @Value("${security.oauth2.client.client-secret}")
    private String clientSecret;

    @DubboReference
    private UserServiceInterface userServiceInterface;

    @Resource
    private OAuthExceptionHandler oAuthExceptionHandler;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    //环绕通知，在RefreshTokenHandle.filter()方法执行前后执行
    @Around("execution(* com.mc.gateway.handle.RefreshTokenHandle.filter(..))")
    public Object refreshToken(ProceedingJoinPoint joinPoint) {
        //获取到filter()方法的参数
        Object[] args = joinPoint.getArgs();
        ServerWebExchange exchange = (ServerWebExchange) args[0];
        String refreshToken = (String) args[2];
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        ObjectMapper objectMapper = new ObjectMapper();
        if (StringUtils.isNotBlank(refreshToken)) {
            // 创建请求参数
            MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
            paramMap.add(OAuthConstants.GRANT_TYPE, OAuthConstants.REFRESH_TOKEN);
            paramMap.add(OAuthConstants.REFRESH_TOKEN, refreshToken);
            paramMap.add(OAuthConstants.CLIENT_ID, clientId);
            paramMap.add(OAuthConstants.CLIENT_SECRET, clientSecret);
            // 创建HttpHeaders实例
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); //在请求头设置内容类型为application/x-www-form-urlencoded，即表单提交
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(paramMap, headers);
            // 发送请求，刷新token
            ResponseEntity<String> refreshResponse = restTemplate.postForEntity(OAuthConstants.REFRESH_TOKEN_URL, request, String.class);
            Object refreshMap = null;
            // 将refreshResponse转换为Map
            Map refreshResponseMap = null;
            try {
                refreshResponseMap = objectMapper.readValue(refreshResponse.getBody(), Map.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            // 判断刷新token是否成功
            if (!refreshResponseMap.get(CommonConstants.CODE).equals(200)) {
                // 刷新token失败，重新登录
                return oAuthExceptionHandler.writeError(exchange, Http.LOGIN_EXPIRED.getMessage());
            } else {
                try {
                    // 刷新token成功，校验用户权限
                    refreshMap = objectMapper.readValue(refreshResponse.getBody(), Map.class).get("data");
                    String token = ((LinkedHashMap) refreshMap).get("token").toString();
                    String checkTokenUrl = OAuthConstants.CHECK_TOKEN_URL.concat(token);
                    // 发送远程请求，获取用户信息
                    ResponseEntity<String> entity = restTemplate.getForEntity(checkTokenUrl, String.class);
                    String requestPath = exchange.getRequest().getPath().value().replace("/api", ""); // 获取请求路径
                    TokenCheckInfo tokenCheckInfo = objectMapper.readValue(entity.getBody(), TokenCheckInfo.class);
                    // 获取用户权限
                    Set<String> permissionList = new HashSet<>();
                    // 如果权限缓存为空的话重建缓存
                    try {
                        if (ObjectUtils.isEmpty(RedisUtil.hashGet(CacheConstants.ROLE_PERMISSION, CacheConstants.ROLE_HASH_KEY_TWO))) {
                            CompletableFuture<ResponseResult<Map<Integer, List<String>>>> responseResult =
                                    userServiceInterface.getRolePermissionList()
                                            .whenCompleteAsync((result, throwable) -> {
                                                if (!result.getCode().equals(CommonConstants.SUCCESS_CODE)) {
                                                    throw new RuntimeException(Http.ROLE_PERMISSION_INIT_FAIL.getMessage());
                                                }
                                            }, threadPoolExecutor);
                            // 获取角色权限列表
                            Map<Integer, List<String>> rolePermissionMap = responseResult.get().getData();
                            rolePermissionMap.forEach((k, v) -> {
                                RedisUtil.hashPut(CacheConstants.ROLE_PERMISSION, CacheConstants.ROLE_HASH_KEY + k, String.join(",", v));
                            });
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(Http.ROLE_PERMISSION_INIT_FAIL.getMessage());
                    }
                    tokenCheckInfo.getRoles().forEach(roleId -> {
                        String permission = (String) RedisUtil.hashGet(CacheConstants.ROLE_PERMISSION, CacheConstants.ROLE_HASH_KEY + roleId);
                        if (StringUtils.isNotBlank(permission)) {
                            permissionList.addAll(Arrays.asList(permission.split(",")));
                        }
                    });
                    // 判断用户权限，如果没有权限，直接返回
                    if (!permissionList.contains(requestPath)) {
                        return oAuthExceptionHandler.writeError(exchange, Http.NOT_PERMISSION.getMessage());
                    }
                    // 将refreshMap转换为JSON字符串
                    String refreshInfo = objectMapper.writeValueAsString(refreshMap);
                    // 将refreshInfo添加到请求头中,使得下游服务通过拦截器获取到refreshInfo
                    exchange.getRequest().mutate().header(OAuthConstants.REFRESH_INFO, refreshInfo).build();
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        // 执行目标方法(执行filter()，放行请求到下游服务)
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
