package com.mc.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

@Configuration
public class CorsConfig {

    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedHeader("*"); // 允许任何请求头
        corsConfiguration.addAllowedMethod("*"); // 允许任何请求方法
        corsConfiguration.addAllowedOriginPattern("*"); // 允许任何请求来源
        corsConfiguration.setAllowCredentials(true); // 允许携带cookie
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration); // 对所有请求路径生效
        return new CorsWebFilter(source);
    }
}
