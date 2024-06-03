package com.mc.auth.service.impl;

import com.mc.common.constants.CommonConstants;
import com.mc.common.dubbo.UserServiceInterface;
import com.mc.common.entity.response.ResponseResult;
import com.mc.common.entity.table.User;
import com.mc.auth.entity.LoginUser;
import com.mc.common.enums.Http;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;


@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @DubboReference(timeout = 2000)
    private UserServiceInterface userServiceInterface;

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 异步调用用户服务接口
        CompletableFuture<ResponseResult<User>> response = userServiceInterface.getUserByUsername(username)
                .whenCompleteAsync((result, throwable) -> {
            if (!result.getCode().equals(CommonConstants.SUCCESS_CODE)
                    || ObjectUtils.isEmpty(result.getData())) {
                throw new RuntimeException(result.getMessage());
            }
        }, threadPoolExecutor);
        User user = null;
        try {
            // 获取用户信息
            user = response.get().getData();
        } catch (Exception e) {
            throw new RuntimeException(Http.USER_INFO_FAIL.getMessage());
        }
        //查询用户角色
        CompletableFuture<ResponseResult<List<Integer>>> userRole = userServiceInterface.getUserRole(user.getId());
        List<Integer> roles = null;
        try {
            roles = userRole.get().getData();
        } catch (Exception e) {
            throw new RuntimeException(Http.GET_USER_ROLE_FAIL.getMessage());
        }
        return new LoginUser(user, roles);
    }
}
