package com.mc.common.constants;

public class NumberConstants {

    public static final int ONE_HUNDRED_THOUSAND = 100000;

    public static final int SIX_NINE = 999999;

    /**
     * 超时时间已过期
     */
    public static final Long EXPIRE_TIME_STATUS = -2L;

    /**
     * 验证码有效时间 10分钟
     */
    public static final Integer EXPIRE_TIME = 600;

    /**
     * 可再次发送验证码时间 9分钟（一分钟后可再次发送）
     */
    public static final Integer RESEND_TIME = 540;

    /**
     * 原子操作（1）
     */
    public static final Double ATOMIC_DOUBLE = 1.0;

    /**
     * 原子操作（-1）
     */
    public static final Double ATOMIC_MINUS_DOUBLE = -1.0;

    /**
     * 0
     */
    public static final Integer ZERO = 0;

    /**
     * 1
     */
    public static final Integer ONE = 1;

    /**
     * -1
     */
    public static final Integer MINUS_ONE = -1;

    /**
     * 2
     */
    public static final Integer TWO = 2;

    /**
     * dubbo远程调用最大容忍时间
     */
    public  static final Integer FIVE = 5;
}
