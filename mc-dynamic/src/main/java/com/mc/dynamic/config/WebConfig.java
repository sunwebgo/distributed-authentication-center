package com.mc.dynamic.config;

import com.mc.dynamic.interceptor.OAuthRefreshTokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Resource
    private OAuthRefreshTokenInterceptor oAuthRefreshTokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(oAuthRefreshTokenInterceptor) //用于拦截所有上游请求，判断请求头中是否携带refresh_info
                .addPathPatterns("/**");
    }
}
