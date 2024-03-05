package com.mc.auth.config;

import com.mc.auth.entity.ClientOAuth2Data;
import com.mc.auth.service.impl.UserDetailsServiceImpl;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import javax.annotation.Resource;

/**
 * @author Xu huaiang
 * @description 授权服务配置
 * @date 2024/02/11
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    // RedisTokenSore
    @Resource
    private RedisTokenStore redisTokenStore;
    // 认证管理对象
    @Resource
    private AuthenticationManager authenticationManager;
    // 密码编码器
    @Resource
    private BCryptPasswordEncoder passwordEncoder;
    // 客户端配置类
    @Resource
    private ClientOAuth2Data clientOAuth2Data;
    // 登录校验
    @Resource
    private UserDetailsServiceImpl userDetailsService;
    @Resource
    private TokenEnhancerConfig tokenEnhancerConfig;


    /**
     * @param clients
     * @throws Exception
     * @description 客户端配置
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory() // 使用内存存储客户端信息
                .withClient(clientOAuth2Data.getClientId()) // 客户端ID
                .secret(passwordEncoder.encode(clientOAuth2Data.getClientSecret())) // 客户端安全码
                .authorizedGrantTypes(clientOAuth2Data.getGrantType()) // 授权类型
                .accessTokenValiditySeconds(clientOAuth2Data.getAccessTokenValiditySeconds()) // token 有效期
                .refreshTokenValiditySeconds(clientOAuth2Data.getRefreshTokenValiditySeconds()) // 刷新 token 的有效期
                .scopes(clientOAuth2Data.getScope()) // 客户端访问范围
                .autoApprove(true); // 自动授权
    }

    /**
     * @param endpoints
     * @throws Exception
     * @deprecated 配置令牌访问端点和令牌服务
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        // 认证器
        endpoints.authenticationManager(authenticationManager)
                // 具体登录的方法
                .userDetailsService(userDetailsService)
                .tokenStore(redisTokenStore)
                .tokenEnhancer(tokenEnhancerConfig)
                // 将 /oauth/token 端点映射到 /login
                .pathMapping("/oauth/token", "/login");
    }

    /**
     * @param security
     * @throws Exception
     * @description 配置令牌端点安全约束
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        security.tokenKeyAccess("permitAll()") // 公开 /oauth/token端点
                .checkTokenAccess("permitAll()") // 公开 /oauth/check_token 端点
                .allowFormAuthenticationForClients(); // 允许表单认证
    }
}
