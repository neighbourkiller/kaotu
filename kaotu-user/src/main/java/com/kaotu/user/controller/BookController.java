package com.kaotu.user.controller;

import com.kaotu.base.context.UserContext;
import com.kaotu.base.exception.BaseException;
import com.kaotu.base.model.vo.BookVO;
import com.kaotu.base.model.vo.CategoryVO;
import com.kaotu.base.model.vo.CommentVO;
import com.kaotu.base.model.vo.CommentVO_;
import com.kaotu.base.result.Result;
import com.kaotu.user.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/user/book")
@Tag(name = "BookController", description = "图书相关接口")
public class BookController {

    @Autowired
    private BookService bookService;

    @GetMapping
    @Operation(summary = "获取书籍信息", description = "根据书籍ID获取书籍详细信息")
    public Result<BookVO> getBookById(@RequestParam Integer bookId){
        try {
            if (bookId == null || bookId <= 0) {
                return Result.error("无效的书籍ID");
            }
            BookVO book = bookService.getBookVOById(bookId);
            return Result.success(book);
        } catch (BaseException e) {
            return Result.error("获取书籍信息失败: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    @Operation(summary = "搜索书籍", description = "根据关键词搜索书籍")
    public Result<List<BookVO>> searchBook(@RequestParam String searchparam){
        try {
            return Result.success(bookService.searchBooks(searchparam));
        }catch (BaseException e){
            return Result.error("搜索书籍失败: " + e.getMessage());
        }
    }

    @GetMapping("/category")
    @Operation(summary = "获取分类下的书籍", description = "根据分类ID获取书籍列表")
    public Result<List<BookVO>> getBookByCategoryId(@RequestParam Integer categoryId) {
        try {
            List<BookVO> list = bookService.getBookListByCategoryId(categoryId);
            return Result.success(list);
        }catch (BaseException e){
            return  Result.error(e.getMessage());
        }
    }

    @GetMapping("/tags")
    public Result<List<CategoryVO>> getAllTags() {
        return Result.success(bookService.getAllCategories(UserContext.getUserId()));
    }

    @GetMapping("/hot")
    public Result<List<BookVO>> getHotList() {
        try {
            List<BookVO> hotBooks = bookService.getHotList();
            return Result.success(hotBooks);
        } catch (BaseException e) {
            log.error("Error fetching hot books: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/comments")
    @Operation(summary = "获取书籍评论", description = "根据书籍ID获取评论列表")
    public Result<List<CommentVO_>> getCommentsByBookId(@RequestParam("bookId") Integer bookId) {
        try {
            List<CommentVO_> comments = bookService.getCommentsByBookId(bookId);
            return Result.success(comments);
        } catch (BaseException e) {
            return Result.error("获取书籍评论失败: " + e.getMessage());
        }
    }
}
