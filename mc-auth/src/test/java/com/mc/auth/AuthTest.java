package com.mc.auth;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import javax.annotation.Resource;
import java.util.*;

@SpringBootTest
public class AuthTest {
    @Resource
    private BCryptPasswordEncoder cryptPasswordEncoder;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Test
    public void contextLoads() {
        System.out.println(cryptPasswordEncoder.encode("1"));
    }

}
