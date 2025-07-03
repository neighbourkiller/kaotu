package com.kaotu.admin.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kaotu.admin.mapper.UserMapper;
import com.kaotu.base.exception.BaseException;
import com.kaotu.admin.mapper.AdminMapper;
import com.kaotu.base.model.dto.PageParams;
import com.kaotu.base.model.dto.UserUpdateDto;
import com.kaotu.base.model.po.Admin;
import com.kaotu.admin.service.AdminService;
import com.kaotu.base.model.po.User;
import com.kaotu.base.model.vo.BookVO;
import com.kaotu.base.model.vo.UserInfo;
import com.kaotu.base.result.PageResult;
import com.kaotu.base.utils.JwtUtil;
import com.kaotu.base.utils.PasswordUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Slf4j
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
        Page<User> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("user_id", "username", "email", "unblock_time", "create_date", "last_login");
        Page<User> pageResult = userMapper.selectPage(page, queryWrapper);
        return new PageResult<>(pageResult.getRecords(), pageResult.getTotal(), pageParams.getPageNo(), pageParams.getPageSize());
    }

    @Override
    public void updateUser(UserUpdateDto userUpdateDto) throws Exception {
        User user = userMapper.selectById(userUpdateDto.getUserId());
        if (user == null) {
            throw new BaseException("用户不存在");
        }
        String password;
        if (userUpdateDto.getPassword() == null || userUpdateDto.getPassword().isEmpty()) {
            password = user.getPassword();
        } else {
            password = PasswordUtil.hashPassword(userUpdateDto.getPassword(), user.getSalt());
        }
        // 更新用户信息
        log.info("更新用户信息: {}", userUpdateDto);
        user.setUsername(userUpdateDto.getUsername());
        user.setPassword(password);
        user.setEmail(userUpdateDto.getEmail());
        if (userUpdateDto.getUnblockTime() == null) {
            user.setUnblockTime(null);
            userMapper.updateUnblockTimeNULL(user.getUserId());
        } else user.setUnblockTime(userUpdateDto.getUnblockTime());
        log.info("----------------更新用户-------------------------");
        log.info("更新后的用户信息: {}", user);
        int rows = userMapper.updateById(user);
        if (rows == 0) {
            throw new BaseException("更新用户信息失败");
        }
    }

    @Override
    public void deleteUser(String userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BaseException("用户不存在");
        }
        int rows = userMapper.deleteById(userId);
        if (rows == 0) {
            throw new BaseException("删除用户失败");
        }
    }

    @Override
    public PageResult<BookVO> listBooks(PageParams pageParams) {
        Page<BookVO> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        QueryWrapper<BookVO> queryWrapper = new QueryWrapper<>();
        Page<BookVO> bookVOPage = adminMapper.selectBookPageWithCategory(page, queryWrapper);
        return new PageResult<>(bookVOPage.getRecords(), bookVOPage.getTotal(), pageParams.getPageNo(), pageParams.getPageSize());
    }
}