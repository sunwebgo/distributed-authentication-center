package com.mc.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * 线程池配置
 * @author Xu huaiang
 * @date 2024/03/18
 */
@Configuration
public class ThreadPoolConfig {

    @Value("${thread-pool.core-size}")
    private Integer coreSize;

    @Value("${thread-pool.max-size}")
    private Integer maxSize;

    @Value("${thread-pool.keep-alive-time}")
    private Integer keepAliveTime;

    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        return new ThreadPoolExecutor(
                coreSize,
                maxSize,
                keepAliveTime,
                TimeUnit.SECONDS,
                new LinkedBlockingDeque<>(100000),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );
    }
}
