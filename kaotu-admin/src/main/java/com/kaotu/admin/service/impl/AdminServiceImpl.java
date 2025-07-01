package com.kaotu.admin.service.impl;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kaotu.admin.mapper.UserMapper;
import com.kaotu.base.exception.BaseException;
import com.kaotu.admin.mapper.AdminMapper;
import com.kaotu.base.model.dto.PageParams;
import com.kaotu.base.model.po.Admin;
import com.kaotu.admin.service.AdminService;
import com.kaotu.base.model.po.User;
import com.kaotu.base.result.PageResult;
import com.kaotu.base.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;

    @Autowired
    private UserMapper userMapper;

    @Value("${kaotu.jwt.secret-key}")
    private String SECRET_KEY;

    @Value("${kaotu.jwt.ttl}")
    private Long TTL;

    @Override
    public String login(Admin admin) {
        Admin adminFromDb = adminMapper.queryAdminById(admin.getAdminId());

        // 检查用户是否存在
        if (adminFromDb == null) {
//            return "用户不存在";
            throw new BaseException("账号或密码错误");
        }

        // 验证密码是否匹配
        // 注意：在生产环境中，密码应该是加密存储的，这里需要使用加密匹配器进行比较
        if (adminFromDb.getPassword().equals(admin.getPassword())) {
            // 登录成功。在实际项目中，这里通常会生成并返回一个JWT token。
            Map<String, Object> claims = new HashMap<>();
            claims.put("adminId", adminFromDb.getAdminId());
            return JwtUtil.createJWT(SECRET_KEY, TTL, claims);
        } else {
//            return "密码错误";
            throw new BaseException("账号或密码错误");
        }
    }

    @Override
    public PageResult<User> listUsers(PageParams pageParams) {
        Page<User> page=new Page<>(pageParams.getPageNo(),pageParams.getPageSize());
        Page<User> pageResult = userMapper.selectPage(page, null);
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal(), pageParams.getPageNo(), pageParams.getPageSize());
    }
}
