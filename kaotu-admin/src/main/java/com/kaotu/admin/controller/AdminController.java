package com.kaotu.admin.controller;

import com.kaotu.admin.model.dto.CommentByBookIdDto;
import com.kaotu.admin.model.dto.CommentStatus;
import com.kaotu.admin.model.dto.SearchPageDto;
import com.kaotu.admin.model.dto.UserPageDto;
import com.kaotu.base.exception.BaseException;
import com.kaotu.base.model.dto.PageParams;
import com.kaotu.base.model.dto.UserUpdateDto;
import com.kaotu.base.model.po.Admin;
import com.kaotu.base.model.po.Book;
import com.kaotu.base.model.po.User;
import com.kaotu.base.model.vo.BookVO;
import com.kaotu.base.model.vo.CommentVO;
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
        log.info("登录请求，管理员信息: {}", admin);
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
    public PageResult<User> listUsers(@RequestBody UserPageDto userPageDto){

        PageResult<User> result= adminService.listUsers(userPageDto);
        log.info(result.toString());
        return result;
    }

    @PutMapping("/user")
    @Operation(summary = "更新用户信息", description = "更新指定用户的信息")
    public Result updateUser(@RequestBody UserUpdateDto userUpdateDto) {
        log.info("更新用户信息，参数: {}", userUpdateDto);

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
    public PageResult<BookVO> listBooks(@RequestBody SearchPageDto pageParams){
        log.info("获取书籍列表，分页参数: {}", pageParams);
        return  adminService.listBooks(pageParams);
    }

    @PutMapping("/book/modify")
    @Operation(summary = "修改书籍信息", description = "修改指定书籍的信息")
    public Result modifyBook(@RequestBody Book book) {
        try {
            adminService.modifyBook(book);
            return Result.success();
        } catch (BaseException e) {
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/book")
    @Operation(summary = "删除书籍", description = "根据书籍ID删除指定书籍")
    public Result deleteBook(@RequestParam Integer bookId) {
        try {
            adminService.deleteBook(bookId);
            return Result.success();
        } catch (BaseException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/book/comments")
    @Operation(summary = "分页查询评论", description = "分页查询书籍评论")
    public PageResult<CommentVO> commentsPage(@RequestBody SearchPageDto searchPageDto){
        log.info("分页查询评论，参数: {}", searchPageDto);
        try {
            return adminService.commentsPage(searchPageDto);
        } catch (BaseException e) {
            log.error("查询评论失败: {}", e.getMessage());
            return null;
        }
    }

    @PostMapping("/book/comment")
    @Operation(summary = "根据bookId查询评论",description = "根据书籍ID分页查询评论")
    public PageResult<CommentVO> commentsByBookId(@RequestBody CommentByBookIdDto commentByBookIdDto) {
        log.info("根据bookId查询评论，参数: {}", commentByBookIdDto);
        try {
            return adminService.commentsPageByBookId(commentByBookIdDto);
        } catch (BaseException e) {
            log.error("查询评论失败: {}", e.getMessage());
            return null;
        }
    }

    @PutMapping("/book/comment")
    @Operation(summary = "更新评论状态", description = "更新指定评论的状态")
    public Result updateCommentStatus(@RequestBody CommentStatus commentStatus) {
        try {
            log.info("更新评论状态，参数: {}", commentStatus);
            adminService.updateCommentStatus(commentStatus);
            return Result.success();
        } catch (BaseException e) {
            log.error("更新评论状态失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @DeleteMapping("/book/comment")
    @Operation(summary = "删除评论", description = "根据评论ID删除指定评论")
    public Result deleteComment(@RequestParam Long commentId) {
        try {
            log.info("删除评论，评论ID: {}", commentId);
            adminService.deleteComment(commentId);
            return Result.success();
        } catch (BaseException e) {
            log.error("删除评论失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
}
