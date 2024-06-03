package com.mc.auth.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mc.auth.entity.TokenEvidence;
import com.mc.common.constants.OAuthConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class OAuthNewTokenInterceptor implements HandlerInterceptor {

    public static ThreadLocal<TokenEvidence> tokenThreadLocal = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取到请求头当中的refresh_info
        String refreshInfo = request.getHeader(OAuthConstants.REFRESH_INFO);
        if (StringUtils.isNotBlank(refreshInfo)) {
            // 将refresh_info转换为map
            ObjectMapper objectMapper = new ObjectMapper();
            Map refreshMap = objectMapper.readValue(refreshInfo, Map.class);
            // 将new_token放入到线程局部变量tokenThreadLocal中
            tokenThreadLocal.set(new TokenEvidence(Boolean.TRUE, (String) refreshMap.get(OAuthConstants.TOKEN)));
        } else {
            //这里获取token的目的是为了对比客户端传递的token参数是否和请求头中的token一致
            String token = request.getHeader(OAuthConstants.AUTHORIZATION);
            if (StringUtils.isNotBlank(token)) {
                tokenThreadLocal.set(new TokenEvidence(Boolean.FALSE, token.replace(OAuthConstants.BEARER, "")));
            }
        }
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }
}
