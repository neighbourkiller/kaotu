package com.kaotu;

import com.kaotu.base.model.po.User;
import com.kaotu.user.mapper.UserMapper;
import com.kaotu.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class KaotuUserApplicationTests {

    @Test
    void contextLoads() {
    }

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserService userService;

    @Test
    void testSelectById() {

        User user = userMapper.selectById("10000000");
        System.out.println(user);
    }

    @Test
    void testLogin() {
        User user = new User();
        user.setUserId("10000000");
        user.setPassword("123456");
        String token = userService.login(user);
        System.out.println(token);
    }
}
