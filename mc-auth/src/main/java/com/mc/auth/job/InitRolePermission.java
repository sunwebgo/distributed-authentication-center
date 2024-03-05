package com.mc.auth.job;

import com.mc.auth.mapper.LoginUserMapper;
import com.mc.common.constants.CacheConstants;
import com.mc.common.constants.LockConstants;
import com.mc.common.utils.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@EnableAsync // 开启异步
public class InitRolePermission implements CommandLineRunner {

    @Resource
    private LoginUserMapper loginUserMapper;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public void run(String... args) throws Exception {
//        获取到分布式锁
        RLock lock = redissonClient.getLock(LockConstants.INIT_ROLE_PERMISSION_LOCK);
        try {
            lock.lock(LockConstants.LOCK_EXPIRE, TimeUnit.SECONDS);
//        查询角色列表
            List<Integer> roleList = loginUserMapper.getRoleList();
//        查询角色权限
            roleList.forEach(roleId -> {
                List<String> rolePermission = loginUserMapper.getRolePermission(roleId);
                RedisUtil.hashPut(CacheConstants.ROLE_PERMISSION, CacheConstants.ROLE_HASH_KEY + roleId, String.join(",", rolePermission));
            });
            log.info("角色权限初始化成功");
        } finally {
            lock.unlock();
        }
    }
}
