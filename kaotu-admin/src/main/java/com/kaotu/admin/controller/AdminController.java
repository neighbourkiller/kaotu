package com.kaotu.admin.controller;

import com.kaotu.base.exception.BaseException;
import com.kaotu.base.model.dto.PageParams;
import com.kaotu.base.model.po.Admin;
import com.kaotu.base.model.po.User;
import com.kaotu.base.result.PageResult;
import com.kaotu.base.result.Result;
import com.kaotu.admin.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@Tag(name = "AdminController", description = "管理员相关接口")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @PostMapping("/login")
    @Operation(summary = "管理员登录", description = "管理员通过用户名和密码登录，成功后返回JWT令牌")
    public Result<String> login(@RequestBody Admin admin){
        try {
            String token = adminService.login(admin);
            if (token != null) {
                return Result.success(token);
            } else {
                return Result.error("登录失败");
            }
        } catch (BaseException e) {
            // 处理异常并返回错误信息
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/user/list")
    @Operation(summary = "获取用户列表", description = "分页获取用户列表")
    public PageResult<User> listUsers(@RequestBody PageParams pageParams){
        return adminService.listUsers(pageParams);
    }

}
