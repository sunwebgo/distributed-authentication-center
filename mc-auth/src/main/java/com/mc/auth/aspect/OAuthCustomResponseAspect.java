package com.mc.auth.aspect;

import com.mc.common.constants.CommonConstants;
import com.mc.common.constants.OAuthConstants;
import com.mc.common.enums.Http;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Xu huaiang
 * @description OAuth2Token切面：在oauth生成token的时候，添加自定义的响应信息
 * @date 2024/02/22
 */
@Aspect
@Component
public class OAuthCustomResponseAspect {

    @Around("execution(* org.springframework.security.oauth2.provider.endpoint.TokenEndpoint.postAccessToken(..))")
    public ResponseEntity handleOAuthResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        // 获取请求参数
        Object[] args = joinPoint.getArgs();
        // 判断是否是refresh_token模式
        if (StringUtils.isBlank((CharSequence) ((LinkedHashMap<?, ?>) args[1]).get(OAuthConstants.GRANT_TYPE))
                && !OAuthConstants.REFRESH_TOKEN.equals(((LinkedHashMap<?, ?>) args[1]).get(OAuthConstants.GRANT_TYPE))) {
            // 是密码模式，添加 grant_type 和 scope 参数
            for (Object arg : args) {
                if (arg instanceof Map) {
                    Map<String, String> parameters = (Map<String, String>) arg;
                    parameters.put(OAuthConstants.GRANT_TYPE, OAuthConstants.PASSWORD);
                    parameters.put(OAuthConstants.SCOPE, OAuthConstants.ALL);
                }
            }
        }
        ResponseEntity<OAuth2AccessToken> responseEntity = null;
        Map<String, Object> newErrorBody = new HashMap<>();
        newErrorBody.put(CommonConstants.CODE, Http.LOGIN_FAIL.getCode());
        newErrorBody.put(CommonConstants.MESSAGE, Http.LOGIN_FAIL.getMessage());
        try {
            // 执行TokenEndpoint中的postAccessToken方法，获取token
            responseEntity = (ResponseEntity<OAuth2AccessToken>) joinPoint.proceed();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.OK).body(newErrorBody);
        }
        // 获取到原始的响应内容
        OAuth2AccessToken originalBody = responseEntity.getBody();
        // 创建新的响应内容
        Map<String, Object> data = new HashMap<>();
        data.put(OAuthConstants.TOKEN, originalBody.getValue());
        data.put(OAuthConstants.REFRESH_TOKEN, originalBody.getRefreshToken().getValue());
        data.put(OAuthConstants.USERINFO,originalBody.getAdditionalInformation().get(OAuthConstants.USERINFO));
        Map<String, Object> newBody = new HashMap<>();
        newBody.put(CommonConstants.CODE, Http.LOGIN_SUCCESS.getCode());
        newBody.put(CommonConstants.MESSAGE, Http.LOGIN_SUCCESS.getMessage());
        newBody.put(CommonConstants.DATA, data);

        return ResponseEntity.status(HttpStatus.OK).body(newBody);
    }
}
