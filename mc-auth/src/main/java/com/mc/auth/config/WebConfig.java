package com.mc.auth.config;

import com.mc.auth.interceptor.OAuthNewTokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Resource
    private OAuthNewTokenInterceptor oAuthNewTokenInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(oAuthNewTokenInterceptor) //用于拦截所有上游请求，判断请求头中是否携带refresh_info
                .excludePathPatterns("/register/**") //注册接口不需要拦截
                .addPathPatterns("/**");
    }
}
