package com.kaotu.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kaotu.base.model.po.User;


public interface UserService extends IService<User> {

    String register(User user);
    String login(User user);
    void modifyEmail(String userId, String email);
}
