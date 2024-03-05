package com.mc.auth.service.impl;

import com.mc.common.entity.table.User;
import com.mc.auth.entity.LoginUser;
import com.mc.auth.mapper.LoginUserMapper;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
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
        if (StringUtils.isBlank(username)) {
            throw new UsernameNotFoundException("用户名不能为空");
        }
        User user = loginUserMapper.getUserByUsername(username);
        if (ObjectUtils.isEmpty(user)) {
            throw new UsernameNotFoundException("用户不存在");
        }
        //查询用户角色
        List<Integer> roles = loginUserMapper.getUserRole(user.getId());
        return new LoginUser(user, roles);
    }
}
