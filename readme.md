[TOC]

**说明**：<font color='red'>本文《分布式认证中心实现方案》是本人的一些拙见，会存在我未想到的不足之处，还请大佬指出。</font>

**源码地址**：https://github.com/sunwebgo/distributed-authentication-center

**参考**：https://blog.csdn.net/zlbdmm/article/details/118692985

​		  https://blog.csdn.net/qq_35427589/article/details/127340635

​		【黑马程序员Java进阶教程快速入门Spring Security OAuth2.0认证授权】 https://www.bilibili.com/video/BV1VE411h7aL/?share_source=copy_web&vd_source=0b39c0c0ea3977b251975ea88134799d



# 1.实现方案

本实现方案整合`Spring Security`和`OAuth2.0`开放标准，采用的是用户名和密码模式。`token`的存储策略是`Redis`，在网关处对`token`进行校验和用户授权，实现`refresh_token`无感知刷新`token`。

系统模块如下：

| 模块       | 说明                 |
| ---------- | -------------------- |
| mc-gateway | 网关模块             |
| mc-auth    | 认证中心             |
| mc-dynamic | 动态服务（资源服务） |

流程图如下：

![分布式认证流程](https://imagebed-xuhuaiang.oss-cn-shanghai.aliyuncs.com/typora/%E5%88%86%E5%B8%83%E5%BC%8F%E8%AE%A4%E8%AF%81%E6%B5%81%E7%A8%8B.jpg)

# 2.OAuth2

## 2.1OAuth简介

<font color='red'>`OAuth`（开放授权）是一个开放标准，允许用户授权第三方应用访问他们存储在另外的服务提供者上的信息，而不需要将用户名和密码提供给第三方应用或分享他们数据的所有内容。</font>OAuth2.0是OAuth协议的延续版本，但不向后兼容OAuth 1.0即完全废止了OAuth1.0。很多大公司如Google，Yahoo，Microsoft等都提供了OAUTH认证服务。

下边分析一个Oauth2认证的例子，通过例子去理解OAuth2.0协议的认证流程，本例子是网站使用微信认证的过程，这个过程的简要描述如下：

> 1. 客户端请求第三方授权用户进入程序的登录页面，点击微信的图标以微信账号登录系统，用户是自己在微信里信息的资源拥有者。
> 2. **资源拥有者同意给客户端授权**：资源拥有者扫描二维码表示资源拥有者同意给客户端授权，微信会对资源拥有者的身份进行验证， 验证通过后，微信会询问用户是否给授权网站访问自己的微信数据，用户点击“确认登录”表示同意授权，<font color='red'>微信认证服务器会颁发一个授权码，并重定向到网站。</font>
> 3. **客户端获取到授权码，请求认证服务器申请令牌**：客户端应用程序请求认证服务器，请求中携带授权码。
> 4. **认证服务器向客户端响应令牌**：微信认证服务器验证了客户端请求的授权码，如果合法则给客户端颁发令牌，令牌是客户端访问资源的通行证。 此交互过程用户看不到，当客户端拿到令牌后，用户在网站看到已经登录成功。
> 5. **客户端携带令牌访问资源服务器的资源**：网站携带令牌请求访问微信服务器获取用户的基本信息。
> 6. **资源服务器返回受保护资源**：资源服务器校验令牌的合法性，如果合法则向用户响应资源信息内容。

![img](https://imagebed-xuhuaiang.oss-cn-shanghai.aliyuncs.com/typora/1936533-20210515224857655-1818650104.png)

OAauth2.0包括以下角色：

1. **客户端**
   本身不存储资源，需要通过资源拥有者的授权去请求资源服务器的资源，比如：Android客户端、Web客户端（浏览器端）、微信客户端等。
2. **资源拥有者**
   通常为用户，也可以是应用程序，即该资源的拥有者。
3. **授权服务器（也称认证服务器）**
   用于服务提供商对资源拥有的身份进行认证、对访问资源进行授权，认证成功后会给客户端发放令牌 （access_token），作为客户端访问资源服务器的凭据。本例为微信的认证服务器。
4. **资源服务器**
   存储资源的服务器，本例子为微信存储的用户信息。

现在还有一个问题，服务提供商能允许随便一个客户端就接入到它的授权服务器吗？答案是否定的，服务提供商会给准入的接入方一个身份，用于接入时的凭据:

* **client_id**：客户端标识
* **client_secret**：客户端秘钥

因此，准确来说，授权服务器对两种OAuth2.0中的两个角色进行认证授权，分别是资源拥有者、客户端。

## 2.2OAuth2的四种授权模式

OAuth 2.0 定义了四种授权方式，每种方式适用于不同的场景和需求。

### 2.2.1授权码（authorization code）

这是最常用且安全性最高的授权方式。适用于有后端的 Web 应用。流程如下：

> - 用户点击 A 网站提供的链接，跳转到 B 网站并授权用户数据给 A 网站。
> - B 网站返回一个授权码给 A 网站。
> - A 网站使用授权码在后端向 B 网站请求令牌。

1. 资源拥有者打开客户端，客户端要求资源拥有者给予授权，它将浏览器被重定向到授权服务器，重定向时会附加客户端的身份信息。如：

```java
/oauth/authorize?client_id=music-community&response_type=code&scope=all&redirect_uri=http://www.baidu.com
```

参数列表如下：

> - `client_id`：客户端准入标识。
> - `response_type`：授权码模式固定为code。
> - `scope`：客户端权限。
> - `redirect_uri`：跳转uri，当授权码申请成功后会跳转到此地址，并在后边带上code参数（授权码）。

2. 浏览器出现向授权服务器授权页面，之后同意授权。
3. 授权服务器将授权码（AuthorizationCode）转经浏览器发送给client(通过redirect_uri)。
4. 客户端拿着授权码向授权服务器索要访问access_token，请求如下：

```java
/oauth/token? client_id=c1&client_secret=secret&grant_type=authorization_code&code=5PgfcD&redirect_uri=http://www.baidu.com
```

5. 授权服务器返回令牌(access_token)

### 2.2.2隐藏式（implicit）

适用于纯前端应用，没有后端的情况。令牌直接传给前端，但安全性较低，令牌有效期通常只在会话期间内。

> - 用户跳转到 B 网站，登录并同意授权。
> - B 网站将令牌作为 URL 锚点传给 A 网站。

### 2.2.3密码式（password）

用户直接将用户名和密码告知应用，应用使用这些凭据申请令牌。

> - A 网站要求用户提供 B 网站的用户名和密码。
> - A 网站使用这些凭据向 B 网站请求令牌。

```java
/oauth/token?client_id=music-community&client_secret=secret&grant_type=password&username=shangsan&password=123
```

参数列表如下：

> * `client_id`：客户端准入标识。
> * `client_secret`：客户端秘钥。
> * `grant_type`：授权类型，填写password表示密码模式
> * `username`：资源拥有者用户名。
> * `password`：资源拥有者密码。

### 2.2.4客户端凭证（client credentials）

适用于客户端应用，不涉及用户的授权。

> - 第三方应用先备案，获取客户端 ID 和客户端密钥。
> - 应用使用这些凭证直接向授权服务器请求令牌。

# 3.认证中心基础搭建

## 3.1pom依赖

认证中心的pom依赖如下：

```xml
	<dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-oauth2</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-bootstrap</artifactId>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid-spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>org.mybatis.spring.boot</groupId>
            <artifactId>mybatis-spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-redis</artifactId>
        </dependency>
        <dependency>
            <groupId>org.redisson</groupId>
            <artifactId>redisson-spring-boot-starter</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
        </dependency>
    </dependencies>
```

## 3.2application.yaml配置文件

```yaml
server:
  port: 10002

# 数据源配置
spring:
  application:
    name: mc-auth
  datasource:
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/mc?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai
    username: root
    password: xu.123456
  redis:
    port: 6379
    host: 
    password: xu.123456
    database: 0
    timeout: 10000



# mybatis配置
mybatis:
  mapper-locations: classpath:mapper/*.xml #mapper文件路径
  type-aliases-package: com.mc.common.entity #实体类路径
  configuration:
    map-underscore-to-camel-case: true #开启驼峰命名
    log-impl: org.apache.ibatis.logging.stdout.StdOutImpl #打印sql日志
    cache-enabled: true #开启二级缓存

# oauth信息
security:
  oauth2:
    client:
      client-id: music-community #客户端id
      client-secret: xu.123456 #客户端密码
      grant-type: #授权类型
        - password
        - refresh_token
      access-token-validity-seconds: 120 #token有效时间
      refresh-token-validity-seconds: 600 #刷新token有效时间
      scope: all #授权范围
        - all
logging:
  level:
    spring: debug
```

## 3.3创建oauth信息实体类

oauth信息实体类用于读取application.yaml文件当中的oauth信息：

```java
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
```

## 3.4实现UserDetailsService接口

因为采用的是`用户名-密码`模式，所以需要实现`spring security`实现的`UserDetailsService`接口去查询数据库验证用户名和密码：

```java
import com.mc.common.constants.OAuthConstants;
import com.mc.common.entity.table.User;
import com.mc.auth.entity.LoginUser;
import com.mc.auth.mapper.LoginUserMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Resource
    private LoginUserMapper loginUserMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = loginUserMapper.getUserByUsername(username);
        if (ObjectUtils.isEmpty(user)) {
            throw new UsernameNotFoundException(OAuthConstants.USER_NOT_FOUND);
        }
        //查询用户角色
        List<Integer> roles = loginUserMapper.getUserRole(user.getId());
        return new LoginUser(user, roles);
    }
}
```

`LoginUser`实现`UserDetails`接口，封装用户对应的角色编号，用于后续用户的权限校验：

```java
import com.mc.common.entity.table.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginUser implements UserDetails {
    private static final long serialVersionUID = 1L;

    private User user;

    private List<Integer> roleId;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
```

## 3.5Security配置

`SecurityConfig`配置类的作用是注入`BCryptPasswordEncoder`（密码采用`BCrypt`的加密方式）、认证管理器和放行的请求：

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * @return {@link AuthenticationManager}
     * @description 认证管理对象
     * @throws Exception
     */
    @Bean
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable().authorizeRequests()
                // 放行的请求
                .antMatchers("/oauth/**").permitAll()
                // 其他请求必须认证才能访问
                .anyRequest().authenticated();
    }
}
```

## 3.6token配置

token的存储策略就采用redis，创建token配置类`TokenConfig`，注入`RedisTokenStore`，设置token存储前缀：

```java
import com.mc.common.constants.CacheConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import javax.annotation.Resource;

@Configuration
public class TokenConfig {

    @Resource
    private RedisConnectionFactory redisConnectionFactory;

    @Bean
    public RedisTokenStore redisTokenStore() {
        RedisTokenStore redisTokenStore = new RedisTokenStore(redisConnectionFactory);
        // 设置 token 存储前缀
        redisTokenStore.setPrefix(CacheConstants.OAUTH_INFO);
        return redisTokenStore;
    }
}
```

## 3.7token增强配置

通过实现`TokenEnhancer`接口，实现`enhance`方法来对token进行增强。添加字段`用户id`和`用户角色`，用于后续的用户鉴权：

```java
import com.mc.auth.entity.LoginUser;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Xu huaiang
 * @description Token增强器
 * @date 2024/02/15
 */
@Component
public class TokenEnhancerConfig implements TokenEnhancer {

    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        Map<String, Object> info = new HashMap<>();
        info.put("id", loginUser.getUser().getId());
        info.put("roles", loginUser.getRoleId());
        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(info);
        return accessToken;
    }
}
```

## 3.8配置OAuth授权服务配置

### 3.8.1授权服务配置类介绍

<font color='cornflowerblue'>可以用 `@EnableAuthorizationServer` 注解并继承`AuthorizationServerConfigurerAdapter`来配置OAuth2.0 授权服务器。</font>

`AuthorizationServerConfigurerAdapter`要求配置以下几个类，这几个类是由`Spring`创建的独立的配置对象，它们会被`Spring`传入`AuthorizationServerConfigurer`中进行配置。

`AuthorizationServerConfigurerAdapter`类如下：

```java
public class AuthorizationServerConfigurerAdapter extends AuthorizationServerConfigurer {

	@Override
	public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
	}

	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
	}

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
	}

}
```

授权服务配置结构：

```java
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {

    
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        super.configure(security);
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        super.configure(clients);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        super.configure(endpoints);
    }
}
```

重写`AuthorizationServerConfigurerAdapter`类当中的三个方法，每个方法的作用如下：

> - `ClientDetailsServiceConfigurer`：用来配置客户端详情服务。
> - `AuthorizationServerEndpointsConfigurer`：用来配置令牌（token）的访问端点和令牌服务(token services)。
> - `AuthorizationServerSecurityConfigurer`：用来配置令牌端点的安全约束.

### 3.8.2客户端详情、令牌访问端点和令牌访问端点安全约束配置

#### 3.8.2.1客户端详情配置

ClientDetailsServiceConfigurer 能够使用内存或者JDBC来实现客户端详情服务（<font color='red'>这里采用内存的方式</font>）， 客户端详情服务（ClientDetailsService）负责查找ClientDetails，而ClientDetails有几个重要的属性如下列表：

> - `clientId`：（必须的）用来标识客户的Id。
> - `secret`：（需要值得信任的客户端）客户端安全码，如果有的话。
> - `scope`：用来限制客户端的访问范围，如果为空（默认）的话，那么客户端拥有全部的访问范围。
> - `authorizedGrantTypes`：此客户端可以使用的授权类型，默认为空。
> - `authorities`：此客户端可以使用的权限（基于Spring Security authorities）。

客户端详情（Client Details）能够在应用程序运行的时候进行更新，可以通过访问底层的存储服务（例如将客户端详情存储在一个关系数据库的表中，就可以使用 JdbcClientDetailsService）或者通过自己实现 ClientRegistrationService接口（同时你也可以实现 ClientDetailsService 接口）来进行管理。



#### 3.8.2.2令牌访问端点配置

`AuthorizationServerEndpointsConfigurer` 是 Spring Security OAuth2 中的一个配置类，用于配置 OAuth2 授权服务器的端点（endpoints）。通过设置以下属性，我们可以决定支持的授权类型（Grant Types）：

> 1. `authenticationManager`：指定用于验证用户身份的 `AuthenticationManager` 实例。这是必需的，因为授权服务器需要验证用户的凭据。
>
> 2. `tokenStore`：指定用于存储访问令牌的 `TokenStore` 实现类。不同的 `TokenStore` 实现方式决定了令牌的存储位置，如内存、数据库或 Redis，`TokenStore` 的实现类如下：
>
>    ![image-20240212131746305](https://imagebed-xuhuaiang.oss-cn-shanghai.aliyuncs.com/typora/image-20240212131746305.png)
>
>    1. **InMemoryTokenStore**：将 OAuth2 访问令牌保存在内存中，使用 `ConcurrentHashMap` 管理。这是一种简单且轻量级的实现方式
>    2. **JdbcTokenStore**：将 OAuth2 访问令牌存储在数据库中，通常使用关系型数据库（如 MySQL、PostgreSQL）来持久化令牌数据。这样可以实现跨服务器共享令牌信。
>    3. **JwkTokenStore**：用于处理 JSON Web Key Set（JWKS）中的令牌。JWKS 是一种用于安全传输令牌的标准格式，通常与 OpenID Connect 和 OAuth2 配合使用。
>    4. **RedisTokenStore**：将 OAuth2 访问令牌存储在 Redis 数据库中，具有高性能和可扩展性。这对于分布式系统和微服务架构非常有用
>
> 3. **`userDetailsService`**：指定用于加载用户信息的 `UserDetailsService` 实现类。授权服务器需要根据用户名查找用户信息，以便生成令牌。
>
> 4. **`authorizationCodeServices`**：指定用于处理授权码授权类型的服务。授权码授权类型通常用于 Web 应用程序的身份验证流程。
>
> 5. **`implicitGrantService`**：指定用于处理隐式授权类型的服务。隐式授权类型通常用于单页应用程序（SPA）的身份验证流程。
>
> 6. **`tokenGranter`**：指定自定义的 `TokenGranter` 实现类，用于支持自定义的授权类型。例如，你可以实现自己的授权类型，然后在这里注册。

`AuthorizationServerEndpointsConfigurer` 允许我们根据项目需求配置授权服务器的不同端点，以支持不同的授权类型。

**配置授权端点的URL（Endpoint URLs）**：
AuthorizationServerEndpointsConfigurer 这个配置对象有一个叫做 `pathMapping()` 的方法用来配置端点URL链接，它有两个参数：

- 第一个参数：String 类型的，这个端点URL的默认链接。
- 第二个参数：String 类型的，你要进行替代的URL链接。

以上的参数都将以 "/" 字符为开始的字符串，框架的默认URL链接如下列表，可以作为这个 pathMapping() 方法的 第一个参数：

> - /oauth/authorize：授权端点。
> - /oauth/token：令牌端点。
> - /oauth/confirm_access：用户确认授权提交端点。
> - /oauth/error：授权服务错误信息端点。
> - /oauth/check_token：用于资源服务访问的令牌解析端点。
> - /oauth/token_key：提供公有密匙的端点，如果你使用JWT令牌的话。

<font color='red'>那么就通过`pathMapping()` 方法将获取token的接口`/oauth/token`映射到`/login`，即用户登录的url。</font>

#### 3.8.2.3令牌访问端点安全配置

**AuthorizationServerSecurityConfigure**：用来配置令牌端点(Token Endpoint)的安全约束，在 AuthorizationServer中配置如下。	

```java
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        // 允许访问 token 的公钥，默认 /oauth/token_key 是受保护的
        security.tokenKeyAccess("permitAll()")
                // 允许检查 token 的状态，默认 /oauth/check_token 是受保护的
                .checkTokenAccess("permitAll()");
    }
```

### 3.8.3授权服务配置类具体实现

授权服务配置类`AuthorizationServerConfig`具体实现：

```java
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
    //token增强配置
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
            	.reuseRefreshTokens(false) // 不重用刷新令牌,每次刷新都会重新生成一个新的刷新令牌
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

```

自此，认证中心的授权服务配置已经搭建完成，测试通过`/login`接口请求token：

授权类型为`password`：

![image-20240305190340595](https://imagebed-xuhuaiang.oss-cn-shanghai.aliyuncs.com/typora/image-20240305190340595.png)

请求头中添加客户端信息：

请求头中的格式为`client_id:client-secret`的Base64编码格式

![image-20240305190438091](https://imagebed-xuhuaiang.oss-cn-shanghai.aliyuncs.com/typora/image-20240305190438091.png)

请求接口，获取token：

![image-20240305190634229](https://imagebed-xuhuaiang.oss-cn-shanghai.aliyuncs.com/typora/image-20240305190634229.png)

查看redis当中存储的token信息：

![image-20240305200457919](https://imagebed-xuhuaiang.oss-cn-shanghai.aliyuncs.com/typora/image-20240305200457919.png)

## 3.9切面类自定义响应

由于`/login`（`/oauth/token`）的响应格式不是统一响应格式code，data，message格式，所以，通过切面类来自定义`/login`接口响应。

首先查看源码`TokenEndpoint`类，`/oauth/token`请求映射方法就是`postAccessToken`

![image-20240305191655793](https://imagebed-xuhuaiang.oss-cn-shanghai.aliyuncs.com/typora/image-20240305191655793.png)



创建切面类：

采用环绕通知，在执行目标方法`postAccessToken()`之前，添加 grant_type 和 scope 参数（**<font color='red'>密码模式</font>**），`grant_type `为`password`,`scope`是`all`。

在执行目标方法`postAccessToken()`获取到token之后，重新定义响应体，改为统一响应体格式。

```java
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
public class OAuthTokenAspect {

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

        Map<String, Object> newBody = new HashMap<>();
        newBody.put(CommonConstants.CODE, Http.LOGIN_SUCCESS.getCode());
        newBody.put(CommonConstants.MESSAGE, Http.LOGIN_SUCCESS.getMessage());
        newBody.put(CommonConstants.DATA, data);

        return ResponseEntity.status(HttpStatus.OK).body(newBody);
    }
}
```

再次测试获取token：

![image-20240305192350933](https://imagebed-xuhuaiang.oss-cn-shanghai.aliyuncs.com/typora/image-20240305192350933.png)

# 4.网关环境搭建

<font color='red'>网关在这里的主要作用是不仅仅是断言、过滤并路由到指定服务，还需要在网关处对token进行校验、刷新token已经对用户身份进行校验。</font>

## 4.1pom依赖

网关依赖

```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-gateway</artifactId>
        </dependency>
```

## 4.2application.yaml配置文件

```yaml
server:
  port: 10001
spring:
  application:
    name: mc-gateway
  cloud:
    gateway:
      routes:
        # 路由到mc-auth服务
        - id: mc-auth
          uri: lb://mc-auth
          predicates:
            - Path=/api/oauth/**, /api/register, /api/login, /api/logout
          filters:
            - RewritePath=/api/(?<segment>.*), /$\{segment}

        # 路由到mc-dynamic服务
        - id: mc-dynamic
          uri: lb://mc-dynamic
          predicates:
            - Path=/api/dynamic/**
          filters:
            - RewritePath=/api/(?<segment>.*), /$\{segment}

  main:
    web-application-type: reactive
  redis:
    port: 6379
    host: 127.0.01
    password: xu.123456
    database: 0
    timeout: 10000

security:
  oauth2:
    client:
      client-id: music-community
      client-secret: xu.123456

# 配置白名单
secure:
  ignore:
    urls: # 配置白名单路径
      - /api/oauth/**
      - /api/register
      - /api/login
```

## 4.3白名单配置类

```java
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
```

## 4.4跨域配置

```java
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
```

## 4.5RestTemplate配置类

```java
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {
    @LoadBalanced
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

## 4.6/oauth/token原始响应实体类

```java
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * @author Xu huaiang
 * @date 2024/02/15
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class TokenCheckInfo {
    private static final long serialVersionUID = 1L;
    private Integer id;

    @JsonProperty("user_name")
    private String username;

    private List<Integer> roles;

    @JsonProperty("client_id")
    private String clientId;

    private String active;

    private String exp;

    private List<String> scope;

}
```

## 4.7token认证异常处理类

```java
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mc.common.entity.response.ResponseResult;
import com.mc.common.enums.Http;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * @author Xu huaiang
 * @date 2024/02/18
 */
@Component
public class OAuthExceptionHandler {

    public Mono<Void> writeError(ServerWebExchange exchange, String msg) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        ObjectMapper objectMapper = new ObjectMapper();
        ResponseResult responseResult = new ResponseResult();

        responseResult = ResponseResult.error(Http.LOGIN_EXPIRED.getCode(), msg);
        String resultInfoJson = null;
        DataBuffer buffer = null;
        try {
            //将响应对象转换为json字符串
            resultInfoJson = objectMapper.writeValueAsString(responseResult);
            buffer = response.bufferFactory().wrap(resultInfoJson.getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }
        return response.writeWith(Mono.just(buffer));
    }
}
```

## 4.8网关全局过滤器

`Spring Cloud Gateway`本质上就是一个过滤器链，通过实现`gateway`提供的`GlobalFilter`来实现网关过滤器，并通过`Order`来设置该网关过滤器的优先级，其优先级最高，在网关过滤器链最前面。

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mc.common.constants.CacheConstants;
import com.mc.common.constants.OAuthConstants;
import com.mc.common.enums.Http;
import com.mc.common.utils.RedisUtil;
import com.mc.gateway.config.IgnoreUrlsConfig;
import com.mc.gateway.entity.TokenCheckInfo;
import com.mc.gateway.exception.OAuthExceptionHandler;
import com.mc.gateway.handle.RefreshTokenHandle;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author Xu huaiang
 * @date 2024/02/22
 * @description 网关全局过滤器，用于身份校验(校验请求是否携带token（白名单除外），token是否有效，请求路径是否在用户权限范围内)
 */
@Component
public class OAuthGlobalFilter implements GlobalFilter, Ordered {

    @Resource
    private IgnoreUrlsConfig ignoreUrlsConfig;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private OAuthExceptionHandler oAuthExceptionHandler;

    @Resource
    private RefreshTokenHandle refreshTokenHandle;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 判断当前的请求是否在白名单中
        AntPathMatcher pathMatcher = new AntPathMatcher();
        boolean flag = false;
        String path = exchange.getRequest().getURI().getPath();
        for (String url : ignoreUrlsConfig.getUrls()) {
            if (pathMatcher.match(url, path)) {
                flag = true;
                break;
            }
        }
        // 白名单放行
        if (flag) {
            return chain.filter(exchange);
        }
        // 拦截请求，获取请求头中的 token 和 refresh_token
        String token = null;
        String refresh_token = null;
        try {
            token = exchange.getRequest().getHeaders().getFirst(OAuthConstants.AUTHORIZATION).replace(OAuthConstants.BEARER, "");
            refresh_token = exchange.getRequest().getHeaders().getFirst(OAuthConstants.REFRESH_TOKEN).trim();
        } catch (Exception e) {
            return oAuthExceptionHandler.writeError(exchange, Http.NEED_LOGIN.getMessage());
        }
        // 校验 token 是否有效
        String checkTokenUrl = OAuthConstants.CHECK_TOKEN_URL.concat(token);
        try {
            // 发送远程请求，验证 token
            ResponseEntity<String> entity = restTemplate.getForEntity(checkTokenUrl, String.class);
            // token 验证失败（token错误或者是失效）
            if (entity.getStatusCode() != HttpStatus.OK || StringUtils.isBlank(entity.getBody())) {
                return refreshTokenHandle.filter(exchange, chain, refresh_token);
            }
            String requestPath = exchange.getRequest().getPath().value(); // 获取请求路径
            ObjectMapper objectMapper = new ObjectMapper();
            TokenCheckInfo tokenCheckInfo = objectMapper.readValue(entity.getBody(), TokenCheckInfo.class);
            // 获取用户权限
            Set<String> permissionList = new HashSet<>();
            tokenCheckInfo.getRoles().forEach(roleId -> {
                String permission = (String) RedisUtil.hashGet(CacheConstants.ROLE_PERMISSION, CacheConstants.ROLE_HASH_KEY + roleId);
                if (StringUtils.isNotBlank(permission)) {
                    permissionList.addAll(Arrays.asList(permission.split(",")));
                }
            });
            // 判断用户权限
            if (!permissionList.contains(requestPath)) {
                return oAuthExceptionHandler.writeError(exchange, Http.NOT_PERMISSION.getMessage());
            }
        } catch (Exception e) {
            return refreshTokenHandle.filter(exchange, chain, refresh_token);
        }
        // 放行
        return chain.filter(exchange);
    }

    /**
     * 网关过滤器的排序，数字越小优先级越高
     *
     * @return
     */
    @Override
    public int getOrder() {
        return -1;
    }
}
```

## 4.9refresh_token无感刷新token

在生产环境中，`refresh_token`的过期时间要比`token`长的多。当`token`过期时，可以通过`refresh_token`来获取新的`token`。`grant_type`为`refresh_token`模式。

在本系统中`refresh_token`实现无感刷新的方案是：

> 1. 通过oauth提供的校验token的接口`/oauth/check_token?token=******`来验证token是否错误或者是失效。
> 2. 如果验证不通过，则由`RefreshTokenHandle`类来处理

`RefreshTokenHandle`类：

```java
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class RefreshTokenHandle {
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain, String refreshToken) {
        return chain.filter(exchange);
    }
}
```

但是这个类本身就是为了放行请求到下游服务，`refresh_token`的操作则由切面类`RefreshTokenAspect`：

切面类`RefreshTokenAspect`采用的是环绕通知，在目标方法`filter()`执行之前，发送HTTP请求通过`refresh_token`来获取新的`token`。如果刷新token成功，则通过`joinPoint.proceed()`执行目标方法(执行filter()，放行请求到下游服务)，而如果刷新失败（`refresh_token`因过期或者是错误无效），则不放行请求到下游服务，实现网关处拦截，通过`oAuthExceptionHandler.writeError`来提示用户重新登录。

```java
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mc.common.constants.CommonConstants;
import com.mc.common.constants.OAuthConstants;
import com.mc.common.enums.Http;
import com.mc.gateway.exception.OAuthExceptionHandler;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @author Xu huaiang
 * @description 刷新token切面，目的是在token过期，但是refresh_token未过期的情况下，
 * 通过refresh_token刷新token，刷新成功后，
 * 将refresh_token和新的token封装到请求响应体中。
 * @date 2024/03/04
 */
@Aspect
@Component
public class RefreshTokenAspect {
    @Resource
    private RestTemplate restTemplate;

    @Value("${security.oauth2.client.client-id}")
    private String clientId;

    @Value("${security.oauth2.client.client-secret}")
    private String clientSecret;

    @Resource
    private OAuthExceptionHandler oAuthExceptionHandler;

    //环绕通知，在RefreshTokenHandle.filter()方法执行前后执行
    @Around("execution(* com.mc.gateway.handle.RefreshTokenHandle.filter(..))")
    public Object refreshToken(ProceedingJoinPoint joinPoint) {
        //获取到filter()方法的参数
        Object[] args = joinPoint.getArgs();
        ServerWebExchange exchange = (ServerWebExchange) args[0];
        String refreshToken = (String) args[2];
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        ObjectMapper objectMapper = new ObjectMapper();
        if (StringUtils.isNotBlank(refreshToken)) {
            // 创建请求参数
            MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
            paramMap.add(OAuthConstants.GRANT_TYPE, OAuthConstants.REFRESH_TOKEN);
            paramMap.add(OAuthConstants.REFRESH_TOKEN, refreshToken);
            paramMap.add(OAuthConstants.CLIENT_ID, clientId);
            paramMap.add(OAuthConstants.CLIENT_SECRET, clientSecret);
            // 创建HttpHeaders实例
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); //在请求头设置内容类型为application/x-www-form-urlencoded，即表单提交
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(paramMap, headers);
            // 发送请求，刷新token
            ResponseEntity<String> refreshResponse = restTemplate.postForEntity(OAuthConstants.REFRESH_TOKEN_URL, request, String.class);
            Object refreshMap = null;
            // 将refreshResponse转换为Map
            Map refreshResponseMap = null;
            try {
                refreshResponseMap = objectMapper.readValue(refreshResponse.getBody(), Map.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            // 判断刷新token是否成功
            if (!refreshResponseMap.get(CommonConstants.CODE).equals(200)) {
                // 刷新token失败，重新登录
                return oAuthExceptionHandler.writeError(exchange, Http.LOGIN_EXPIRED.getMessage());
            } else {
                try {
                    refreshMap = objectMapper.readValue(refreshResponse.getBody(), Map.class).get("data");
                    // 将refreshMap转换为JSON字符串
                    String refreshInfo = objectMapper.writeValueAsString(refreshMap);
                    // 将refreshInfo添加到请求头中
                    exchange.getRequest().mutate().header(OAuthConstants.REFRESH_INFO, refreshInfo).build();
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        // 执行目标方法(执行filter()，放行请求到下游服务)
        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
```

## 4.10token无感刷新测试

1. 首先进行登录

![image-20240305202404117](https://imagebed-xuhuaiang.oss-cn-shanghai.aliyuncs.com/typora/image-20240305202404117.png)

2. 查看redis当中的token信息

![image-20240305202446382](https://imagebed-xuhuaiang.oss-cn-shanghai.aliyuncs.com/typora/image-20240305202446382.png)

3. 等待token过期

![image-20240305202602223](https://imagebed-xuhuaiang.oss-cn-shanghai.aliyuncs.com/typora/image-20240305202602223.png)

4. 通过过期的token和未过期的refresh_token发送请求测试

![image-20240305202816593](https://imagebed-xuhuaiang.oss-cn-shanghai.aliyuncs.com/typora/image-20240305202816593.png)

5. 查看redis当中的token信息

![image-20240305202853288](https://imagebed-xuhuaiang.oss-cn-shanghai.aliyuncs.com/typora/image-20240305202853288.png)

可以看到已经实现了token的无感刷新。



# 5.资源服务配置

## 5.1资源服务配置说明

资源服务要做的就是创建拦截器（过滤器也可以），拦截所有请求，判断请求头当中是否存在`refresh_info`：

`refresh_info`就是在网关处通过`refresh_token`生成的新的`token`，并将`refresh_token`和`token`放入到请求头中，传递给下游服务。

```java
                    refreshMap = objectMapper.readValue(refreshResponse.getBody(), Map.class).get("data");
                    // 将refreshMap转换为JSON字符串
                    String refreshInfo = objectMapper.writeValueAsString(refreshMap);
                    // 将refreshInfo添加到请求头中
                    exchange.getRequest().mutate().header(OAuthConstants.REFRESH_INFO, refreshInfo).build();
```

所以在下游服务（资源服务）需要创建拦截器（或者过滤器）拦截所有请求，判断请求头当中是否存在`refresh_info`，如果有就证明`token`无效，并通过`refresh_token`生成了新的`token`，然后将生成的新的`refresh_token`和新的`token`设置到响应头中，然后在前端就能够获取到并设置到`localStorage`。

## 5.2OAuthRefreshTokenInterceptor拦截器

```java
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
            // 将token和refresh_token存入response响应头中
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
```

## 5.3配置拦截器

```java
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
```

# 6.前端操作

## 6.1登录存储token和refresh_token

成功登录后将token和refresh_token存储到`localStorage`中。

```js
login() {
  if (this.$refs.form.validate()) {
    this.snackbar = true;
    let clientId = 'music-community';
    let clientSecret = 'xu.123456';
    let auth = btoa(clientId + ':' + clientSecret); // 对client-id和client-secret进行Base64编码
    this.$http({
      method: "post",
      url: "/login",
      headers: {'Authorization': 'Basic ' + auth},  // 在请求头中添加Authorization字段，值为Basic加上client-id和client-secret的Base64编码
      params: {username: this.username, password: this.password}
    })
        .then(resp => {
          if (resp.data.code === 200) {
            localStorage.setItem('token', resp.data.data.token); // 将token存储到localStorage中
            localStorage.setItem('refreshToken', resp.data.data.refresh_token); // 将refreshToken存储到localStorage中
            this.$router.push("/index/main");
          } else {
            this.loginSnackbar = true;
            this.loginSnackbarMessage = resp.data.message;
            this.$router.push("/login");
          }
        })
        .catch(err => {
          console.log(err);
        });
  }
},
```

## 6.2拦截器

> 1. 请求拦截器的作用是拦截除忽略外的所有请求，向请求头中添加`Authorization`字段和`refresh_token`字段。
> 2. 响应拦截器的作用是拦截所有响应，判断响应头中是否存在新的token和refresh_token，如果存在就存储到`localStorage`中

```js
import axios from 'axios';

// 请求拦截器
axios.interceptors.request.use(config => {
    if (config.url === '/api/login' || config.url === '/api/register') {
        return config;
    } else {
        const token = localStorage.getItem('token');
        const refresh_token = localStorage.getItem('refresh_token');
        if (token && refresh_token) {
            config.headers.Authorization = `Bearer ${token}`;
            config.headers.refresh_token = refresh_token;
        }
        return config;
    }
}, error => {
    return Promise.reject(error);
});

// 响应拦截器
axios.interceptors.response.use(response => {
    const {headers, data} = response;
    const newToken = headers['token']; // Assuming the header name for new token
    const newRefreshToken = headers['refresh-token']; // Assuming the header name for new refresh token

    if (newToken && newRefreshToken) {
        // 存储新的 token 和 refresh_token 到 localStorage
        localStorage.setItem('token', newToken);
        localStorage.setItem('refresh_token', newRefreshToken);
    }
    return response;
}, error => {
    if (error.response.status === 401) {
        // 处理 token 过期等情况
        // router.push('/login');
    }
    return Promise.reject(error);
});
```

