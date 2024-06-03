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
            // 将token存入response响应头中
            // 在处理跨域请求时，浏览器默认只能访问一些基本的响应头，如果你想让前端能够访问到其他的自定义响应头，如token和refresh_token，你需要在服务器端设置Access-Control-Expose-Headers头
            // Access-Control-Expose-Headers是一个HTTP响应头，它允许服务器指定哪些响应头
            // 可以被浏览器中运行的脚本访问，以响应跨源请求。
            // 默认情况下，只有CORS安全列表中的响应头可以被暴露给浏览器。
            // 这些安全列表的响应头包括：Cache-Control、Content-Language、Content-Type、Expires、Last-Modified和Pragma12。
            // 如果你想让客户端可以访问到其他的头信息，服务器必须在Access-Control-Expose-Headers中列出它们。
            // 这个头的值是一个逗号分隔的列表，包含了你想要暴露给前端的响应头的名字。
            response.setHeader(OAuthConstants.ACCESS_CONTROL_EXPOSE_HEADERS, OAuthConstants.TOKEN);
            response.setHeader(OAuthConstants.TOKEN, (String) refreshMap.get(OAuthConstants.TOKEN));
        }
        return HandlerInterceptor.super.preHandle(request, response, handler);
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        HandlerInterceptor.super.postHandle(request, response, handler, modelAndView);
    }
}
