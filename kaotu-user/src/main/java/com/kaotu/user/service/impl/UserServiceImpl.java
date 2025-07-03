package com.kaotu.user.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaotu.base.exception.BaseException;
import com.kaotu.base.model.po.User;
import com.kaotu.base.properties.JwtProperties;
import com.kaotu.base.utils.JwtUtil;
import com.kaotu.base.utils.PasswordUtil;
import com.kaotu.user.mapper.UserMapper;
import com.kaotu.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    public String register(User user) {
        User selected = userMapper.selectById(user.getUserId());
        if (selected != null)
            throw new BaseException("账号已存在");
        if(PasswordUtil.isUserIdValid(user.getUserId()))
            throw new BaseException("账号格式不正确");
        if(PasswordUtil.isValid(user.getPassword()))
            throw new BaseException("密码格式不正确");

        user.setUsername("用户" + user.getUserId());
        //密码加密逻辑
        String salt = UUID.randomUUID().toString();
        user.setSalt(salt);
        user.setPassword(hashPassword(user.getPassword(), salt));

        int insert = userMapper.insert(user);
        if (insert==0)
            throw new BaseException("系统错误");
        Map<String,Object> claims=new HashMap<>();
        claims.put("userId", user.getUserId());
        return JwtUtil.createJWT(jwtProperties.getSecretKey(), jwtProperties.getTtl(), claims);
    }

    /**
     * 用户登录
     * @param user 包含用户ID和本次输入的密码
     * @return 登录成功后返回JWT
     */
    public String login(User user) {
        // 1. 根据userId查询数据库中的用户信息
//        User userInDb = userMapper.getByUserId(user.getUserId());
        log.info("userId: {}", user.getUserId());
        User userInDb = userMapper.selectById(user.getUserId());
        // 2. 判断用户是否存在
        if (userInDb == null) {
            throw new BaseException("账号或密码错误");
        }

        // 3. 对用户输入的密码进行加密，使用从数据库中取出的盐
        String hashedPassword = hashPassword(user.getPassword(), userInDb.getSalt());

        // 4. 将加密后的密码与数据库中的密码进行比对
        if (!hashedPassword.equals(userInDb.getPassword())) {
            throw new BaseException("账号或密码错误");
        }

        userInDb.setLastLogin(LocalDateTime.now());
        int update = userMapper.updateById(userInDb);
        if (update == 0) {
            throw new BaseException("系统错误，登录失败");
        }

        // 5. 密码正确，生成token并返回
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userInDb.getUserId());
        return JwtUtil.createJWT(jwtProperties.getSecretKey(), jwtProperties.getTtl(), claims);
    }

    public void modifyEmail(String userId, String email) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BaseException("用户不存在");
        }
        user.setEmail(email);
        int update = userMapper.updateById(user);
        if (update == 0) {
            throw new BaseException("系统错误，修改邮箱失败");
        }
    }

    /**
     * 使用SHA-256对密码进行哈希处理
     * @param password 原始密码
     * @param salt 盐值
     * @return 哈希后的密码
     */
    private String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // 将密码和盐结合
            String text = password + salt;
            final byte[] hashbytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            // 将字节数组转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte hashbyte : hashbytes) {
                String hex = Integer.toHexString(0xff & hashbyte);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("密码加密失败", e);
            throw new BaseException("系统错误，无法加密密码");
        }
    }
}
