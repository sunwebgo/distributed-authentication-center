package com.mc.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mc.common.constants.CacheConstants;
import com.mc.common.constants.CommonConstants;
import com.mc.common.constants.OAuthConstants;
import com.mc.common.dubbo.UserServiceInterface;
import com.mc.common.entity.response.ResponseResult;
import com.mc.common.enums.Http;
import com.mc.common.utils.RedisUtil;
import com.mc.gateway.config.IgnoreUrlsConfig;
import com.mc.gateway.entity.TokenCheckInfo;
import com.mc.gateway.exception.OAuthExceptionHandler;
import com.mc.gateway.handle.RefreshTokenHandle;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

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

    @DubboReference
    private UserServiceInterface userServiceInterface;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 判断当前的请求是否在白名单中
        AntPathMatcher pathMatcher = new AntPathMatcher();
        boolean flag = false;
        String path = exchange.getRequest().getURI().getPath();
        // 对特殊接口进行判断
        // 如果查询热门音乐接口或者是音乐列表或是动态列表，并且携带了用户id，则不放行（携带用户id，判断用户的点赞情况）
        if ((CommonConstants.API_MUSIC_HOT.equals(path)
                || CommonConstants.API_MUSIC_LIST.equals(path)
                || CommonConstants.API_SEARCH_MUSIC.equals(path)
                || CommonConstants.API_SEARCH_DYNAMIC.equals(path)
                || CommonConstants.API_DYNAMIC_LIST.equals(path)
                || CommonConstants.API_COMMENT_LIST.equals(path))
                && (!exchange.getRequest().getQueryParams().containsKey(CommonConstants.UID)
                || ObjectUtils.isEmpty(exchange.getRequest().getQueryParams().getFirst(CommonConstants.UID)))
        ) {
            return chain.filter(exchange);
        }
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
            if (StringUtils.isBlank(token) || StringUtils.isBlank(refresh_token)) {
                return oAuthExceptionHandler.writeError(exchange, Http.NEED_LOGIN.getMessage());
            }
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
            String requestPath = exchange.getRequest().getPath().value().replace("/api", ""); // 获取请求路径
            ObjectMapper objectMapper = new ObjectMapper();
            TokenCheckInfo tokenCheckInfo = objectMapper.readValue(entity.getBody(), TokenCheckInfo.class);
            // 获取用户权限
            Set<String> permissionList = new HashSet<>();
            // 如果权限缓存为空的话重建缓存
            if (ObjectUtils.isEmpty(RedisUtil.hashGet(CacheConstants.ROLE_PERMISSION, CacheConstants.ROLE_HASH_KEY_TWO))) {
                CompletableFuture<ResponseResult<Map<Integer, List<String>>>> response =
                        userServiceInterface.getRolePermissionList()
                                .whenCompleteAsync((result, throwable) -> {
                                    if (!result.getCode().equals(CommonConstants.SUCCESS_CODE)) {
                                        throw new RuntimeException(Http.ROLE_PERMISSION_INIT_FAIL.getMessage());
                                    }
                                }, threadPoolExecutor);
                // 获取角色权限列表
                Map<Integer, List<String>> rolePermissionMap = response.get().getData();
                rolePermissionMap.forEach((k, v) -> {
                    RedisUtil.hashPut(CacheConstants.ROLE_PERMISSION, CacheConstants.ROLE_HASH_KEY + k, String.join(",", v));
                });
            }
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

