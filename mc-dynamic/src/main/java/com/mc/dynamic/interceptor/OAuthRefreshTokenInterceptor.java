package com.mc.dynamic.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mc.common.constants.OAuthConstants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

@Component
public class OAuthRefreshTokenInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取到请求头当中的refresh_info
        String refreshInfo = request.getHeader(OAuthConstants.REFRESH_INFO);
        if (StringUtils.isNotBlank(refreshInfo)) {
            // 将refresh_info转换为map
            ObjectMapper objectMapper = new ObjectMapper();
            Map refreshMap = objectMapper.readValue(refreshInfo, Map.class);
            // 将token和refresh_token存入response中
            response.setHeader(OAuthConstants.TOKEN, (String) refreshMap.get(OAuthConstants.TOKEN));
            response.setHeader(OAuthConstants.REFRESH_TOKEN, (String) refreshMap.get(OAuthConstants.REFRESH_TOKEN));
        }
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }
}
