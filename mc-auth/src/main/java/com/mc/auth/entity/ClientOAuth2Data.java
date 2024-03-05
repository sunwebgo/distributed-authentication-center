package com.mc.auth.entity;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "security.oauth2.client")
public class ClientOAuth2Data {
    /**
     * 客户端id
     */
    private String clientId;

    /**
     * 客户端密钥
     */
    private String clientSecret;

    /**
     * 授权类型
     */
    private String[] grantType;

    /**
     * token有效期
     */
    private int accessTokenValiditySeconds;

    /**
     * refresh-token有效期
     */
    private int refreshTokenValiditySeconds;

    /**
     * 客户端访问范围
     */
    private String[] scope;
}
