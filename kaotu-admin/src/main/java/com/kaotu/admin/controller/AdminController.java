package com.kaotu.admin.controller;

import com.kaotu.base.exception.BaseException;
import com.kaotu.base.model.dto.PageParams;
import com.kaotu.base.model.dto.UserUpdateDto;
import com.kaotu.base.model.po.Admin;
import com.kaotu.base.model.po.Book;
import com.kaotu.base.model.po.User;
import com.kaotu.base.model.vo.BookVO;
import com.kaotu.base.result.PageResult;
import com.kaotu.base.result.Result;
import com.kaotu.admin.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
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

        PageResult<User> result= adminService.listUsers(pageParams);
        log.info(result.toString());
        return result;
    }

    @PutMapping("/user")
    @Operation(summary = "更新用户信息", description = "更新指定用户的信息")
    public Result updateUser(@RequestBody UserUpdateDto userUpdateDto) {
        try {
            adminService.updateUser(userUpdateDto);
            return Result.success();
        } catch (BaseException e) {
            return Result.error(e.getMessage());
        } catch (Exception e) {
            return Result.error("更新用户信息失败");
        }
    }

    @DeleteMapping("/user")
    @Operation(summary = "删除用户", description = "根据用户ID删除指定用户")
    public Result deleteUser(@RequestParam String userId) {
        try {
            adminService.deleteUser(userId);
            return Result.success();
        } catch (BaseException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/book/list")
    @Operation(summary = "获取书籍列表", description = "分页获取书籍列表")
    public PageResult<BookVO> listBooks(@RequestBody PageParams pageParams){
        log.info("获取书籍列表，分页参数: {}", pageParams);
        return  adminService.listBooks(pageParams);
    }
}
