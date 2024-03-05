package com.mc.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 网关白名单配置
 * @author Xu huaiang
 * @date 2024/02/14
 */
@Data
@Component
@ConfigurationProperties(prefix = "secure.ignore")
public class IgnoreUrlsConfig {
    private String[] urls;
}
