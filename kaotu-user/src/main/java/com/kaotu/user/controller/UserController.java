package com.kaotu.user.controller;

import com.kaotu.base.context.UserContext;
import com.kaotu.base.exception.BaseException;
import com.kaotu.base.model.dto.CommentDto;
import com.kaotu.base.model.po.User;
import com.kaotu.base.model.vo.BookVO;
import com.kaotu.base.model.vo.CommentVO;
import com.kaotu.base.model.vo.CommentVO_;
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

import java.util.List;

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

    @PostMapping("/tag")
    @Operation(summary = "选择标签", description = "用户选择标签")
    public Result selectTags(@RequestParam List<Integer> tags){
        try {
            bookService.addTags(tags);
            return Result.success();
        } catch (BaseException e) {
            log.error("Error selecting tags: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/writeOff")
    @Operation(summary = "注销用户", description = "注销当前登录用户")
    public Result writeOff(){
        try {
            userService.removeById(UserContext.getUserId());
            return Result.success();
        }catch (Exception e){
            log.error("Error writing off user: {}", e.getMessage());
            return Result.error("注销失败，请稍后再试");
        }
    }

    @GetMapping("/logout")
    @Operation(summary = "用户登出", description = "用户登出操作")
    public Result logout(){
        return Result.success();
    }




    @GetMapping("/personalize")
    @Operation(summary = "获取个性化推荐", description = "获取当前用户的个性化书籍推荐")
    public Result<List<BookVO>> personalize() {
        log.info("Fetching personalized recommendations for userId: {}", UserContext.getUserId());
        return Result.success(bookService.getPersonalize());
    }


    @GetMapping("/collect")
    @Operation(summary = "获取收藏书籍", description = "获取当前用户收藏的书籍列表")
    public Result<List<BookVO>> collect() {
        log.info("Fetching collected books for userId: {}", UserContext.getUserId());
        try {
            List<BookVO> collectedBooks = bookService.getCollectBooks();
            return Result.success(collectedBooks);
        } catch (BaseException e) {
            log.error("Error fetching collected books: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/record")
    public Result recordTime(@RequestParam("bookId") Integer bookId,@RequestParam("time") Integer time){
        try {
            userService.recordBrowseTime(bookId, time);
            return Result.success();
        }catch (Exception e){
            log.error("Error recording browse time: {}", e.getMessage());
            return Result.error("记录浏览时间失败，请稍后再试");
        }
    }

    @PostMapping("/comment")
    @Operation(summary = "发表评论", description = "用户对书籍发表评论")
    public Result comment(@RequestBody CommentDto commentDto){
        try {
            userService.commentBook(commentDto);
            return Result.success();
        }catch (BaseException e){
            log.error("Error commenting book: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/mycomments")
    @Operation(summary = "我的评论",description = "查看用户的所有评论，按时间倒序排序")
    public Result<List<CommentVO_>> myComments(){
        return Result.success(userService.myComments());
    }

    @GetMapping("/upvote")
    @Operation(summary = "点赞评论", description = "用户对评论进行点赞")
    public Result upvoteComment(@RequestParam("commentId") Integer commentId) {
        try {
            userService.upvoteComment(commentId);
            return Result.success();
        } catch (BaseException e) {
            log.error("Error upvoting comment: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/upvote")
    @Operation(summary = "取消点赞评论", description = "用户取消对评论的点赞")
    public Result undoVoteComment(@RequestParam("commentId") Integer commentId) {
        try {
            userService.undoVoteComment(commentId);
            return Result.success();
        } catch (BaseException e) {
            log.error("Error undoing upvote on comment: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}
