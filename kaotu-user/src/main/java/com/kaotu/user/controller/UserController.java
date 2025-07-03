package com.kaotu.user.controller;

import com.kaotu.base.context.UserContext;
import com.kaotu.base.exception.BaseException;
import com.kaotu.base.model.po.User;
import com.kaotu.base.model.vo.UserInfo;
import com.kaotu.base.result.Result;
import com.kaotu.user.service.BookService;
import com.kaotu.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/user/user")
@Tag(name = "UserController", description = "用户相关接口")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private BookService bookService;

    @Operation(summary = "用户注册", description = "用户通过用户id和密码注册，成功后返回JWT令牌")
    @PostMapping("/register")
    public Result<String> register(@RequestBody User user) {
        // 这里可以添加用户注册逻辑
        log.info("Registering user: {}", user);
        try {
            String token = userService.register(user);
            return Result.success(token);
        } catch (BaseException e) {
            log.error("Error registering user: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }



    @Operation(summary = "用户登录", description = "用户通过用户id和密码登录，成功后返回JWT令牌")
    @PostMapping("/login")
    public Result<String> login(@RequestBody User user){
        //返回 token
        log.info("Logging in user: {}", user);
        try {
            String token = userService.login(user);
            return Result.success(token);
        } catch (BaseException e) {
            log.error("Error logging in user: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @Operation(summary = "获取用户信息", description = "获取当前登录用户的信息")
    @GetMapping
    public Result<UserInfo> userInfo() {
        log.info("userId:{}", UserContext.getUserId());
        User user=userService.getById(UserContext.getUserId());
        UserInfo userInfo=new UserInfo();
        BeanUtils.copyProperties(user, userInfo);
        return Result.success(userInfo);
    }

    @Operation(summary = "修改用户邮箱", description = "修改当前登录用户的邮箱地址")
    @PutMapping("/email")
    public Result modifyEmail(@RequestParam String email) {
        log.info("Modifying email for userId: {}, new email: {}", UserContext.getUserId(), email);
        try {
            userService.modifyEmail(UserContext.getUserId(), email);
            return Result.success();
        } catch (BaseException e) {
            log.error("Error modifying email: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/favorite")
    @Operation(summary = "收藏书籍", description = "收藏指定的书籍")
    public Result collectBook(@RequestParam Integer bookId) {
        try {
            bookService.collectBook(bookId);
            return Result.success();
        }catch (BaseException e){
            return Result.error("收藏书籍失败: " + e.getMessage());
        }
    }
}
