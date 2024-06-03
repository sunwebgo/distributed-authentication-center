package com.mc.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class AuthTest {

    @Test
    private void testLocalDate() {
        long nowTime = System.currentTimeMillis();
        System.out.println(nowTime);
    }
}
