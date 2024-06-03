package com.mc.common.constants;

import java.util.concurrent.TimeUnit;

/**
 * @author Xu huaiang
 * @description 分布式锁常量
 * @date 2024/02/22
 */
public class LockConstants {
    /**
     *  分布式锁过期时间10秒
     */
    public static final Integer LOCK_EXPIRE_TEN = 10;

    /**
     *  分布式锁过期时间30秒
     */
    public static final Integer LOCK_EXPIRE_THIRTY = 30;

    /**
     *  初始化角色权限锁
     */
    public static final String INIT_ROLE_PERMISSION_LOCK = "initRolePermissionLock";

    /**
     *  初始化Elastic数据锁
     */
    public static final String INIT_ELASTIC_DATA_LOCK = "initElasticDataLock";

    /**
     *  初始化音乐数据锁
     */
    public static final String INIT_MUSIC_DATA_LOCK = "initMusicDataLock";



}
