package com.kaotu.user.controller;

import com.kaotu.base.context.UserContext;
import com.kaotu.base.exception.BaseException;
import com.kaotu.base.model.dto.PostCommentDto;
import com.kaotu.base.model.dto.PostDto;
import com.kaotu.base.model.vo.PostCommentVO;
import com.kaotu.base.model.vo.PostTagVO;
import com.kaotu.base.model.vo.PostVO;
import com.kaotu.base.model.vo.ViewHistory;
import com.kaotu.base.result.Result;
import com.kaotu.base.utils.LogUtils;
import com.kaotu.user.service.CommunityPostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "PostController", description = "帖子相关接口")
@RestController
@RequestMapping("/user/post")
@Slf4j
public class PostController {

    @Autowired
    private CommunityPostService postService;

    @GetMapping("/tags")
    @Operation(summary = "获取帖子标签列表")
    public Result<List<PostTagVO>> getPostTags() {

        log.info("获取帖子标签列表");
        return Result.success(postService.getAllTags());
    }

    @PostMapping("/post")
    @Operation(summary = "发布帖子")
    public Result post(@RequestBody PostDto postDto) {
        log.info("发布帖子: {}", postDto);
        try {
            postService.savePost(postDto);
            return Result.success("帖子发布成功");
        } catch (BaseException e) {
            log.error("发布帖子失败: {}", e.getMessage());
            return Result.error("帖子发布失败: " + e.getMessage());
        }
    }

    @GetMapping("/detail")
    @Operation(summary = "获取帖子详情")
    public Result<PostVO> getPostDetail(@RequestParam Long postId) {
        log.info("获取帖子详情: postId={}", postId);
        try {
            PostVO postDetail = postService.getPostDetail(postId);
            // 记录用户浏览帖子日志
//            LogUtils.other("用户id: {} 浏览-帖子id: {}", UserContext.getUserId(), postId);
            return Result.success(postDetail);
        } catch (BaseException e) {
            log.error("获取帖子详情失败: {}", e.getMessage());
            return Result.error("获取帖子详情失败: " + e.getMessage());
        }
    }

    @GetMapping("/upvote")
    @Operation(summary = "点赞")
    public Result upvotePost(@RequestParam Long targetId, @RequestParam Boolean targetType) {
        log.info("点赞: targetId={}, targetType={}", targetId, targetType);
        try {
            postService.upvotePost(targetId, targetType);
            return Result.success("点赞成功");
        } catch (BaseException e) {
            log.error("点赞失败: {}", e.getMessage());
            return Result.error("点赞失败: " + e.getMessage());
        }
    }

    @GetMapping("/collect")
    @Operation(summary = "收藏帖子")
    public Result collectPost(@RequestParam Long postId) {
        log.info("收藏帖子: postId={}", postId);
        try {
            postService.collectPost(postId);
            return Result.success("帖子收藏成功");
        } catch (BaseException e) {
            log.error("帖子收藏失败: {}", e.getMessage());
            return Result.error("帖子收藏失败: " + e.getMessage());
        }
    }

    @GetMapping("/collected")
    @Operation(summary = "获取用户收藏的帖子")
    public Result<List<PostVO>> getCollectedPosts() {
        log.info("获取用户收藏的帖子");
        try {
            List<PostVO> collectedPosts = postService.getCollectedPosts();
            return Result.success(collectedPosts);
        } catch (BaseException e) {
            log.error("获取用户收藏的帖子失败: {}", e.getMessage());
            return Result.error("获取用户收藏的帖子失败: " + e.getMessage());
        }
    }

    @PostMapping("/comment")
    @Operation(summary = "评论帖子", description = "评论帖子接口，支持回复评论")
    public Result comment(@RequestBody PostCommentDto commentDto) {
        try {
            log.info("评论帖子: {}", commentDto);
            postService.commentPost(commentDto);
            return Result.success("评论成功");
        } catch (BaseException e) {
            log.error("评论帖子失败: {}", e.getMessage());
            return Result.error("评论失败: " + e.getMessage());
        }
    }


    @GetMapping("/new")
    @Operation(summary = "获取最新帖子")
    public Result<List<PostVO>> getNewestPosts() {
        log.info("获取最新帖子");
        try {
            List<PostVO> newestPosts = postService.getNewestPosts();
            return Result.success(newestPosts);
        } catch (BaseException e) {
            log.error("获取推荐帖子失败: {}", e.getMessage());
            return Result.error("获取推荐帖子失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/post")
    @Operation(summary = "删除帖子")
    public Result deletePost(@RequestParam Long postId) {
        log.info("删除帖子: postId={}", postId);
        try {
            postService.removePostById(postId);
            return Result.success("帖子删除成功");
        } catch (BaseException e) {
            log.error("删除帖子失败: {}", e.getMessage());
            return Result.error("删除帖子失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/comment")
    @Operation(summary = "删除评论")
    public Result deleteComment(@RequestParam Long commentId) {
        log.info("删除评论: commentId={}", commentId);
        try {
            // 这里需要实现删除评论的逻辑
            postService.removeCommentById(commentId); // 假设有这个方法
            return Result.success("评论删除成功");
        } catch (BaseException e) {
            log.error("删除评论失败: {}", e.getMessage());
            return Result.error("删除评论失败: " + e.getMessage());
        }
    }

    @GetMapping("/record")
    @Operation(summary = "记录用户浏览帖子日志")
    public Result recordPostView(@RequestParam("postId") Long postId,@RequestParam("time") Integer time){
        log.info("记录用户浏览帖子日志: postId={}, time={}", postId, time);
        try {
            postService.recordPostView(postId, time);
            return Result.success("浏览记录已保存");
        } catch (Exception e) {
            log.error("记录浏览日志失败: {}", e.getMessage());
            return Result.error("记录浏览日志失败: " + e.getMessage());
        }
    }

    @GetMapping("/comments")
    @Operation(summary = "获取帖子评论", description = "根据帖子ID获取评论列表")
    public Result<List<PostCommentVO>> getCommentsByPostId(@RequestParam("postId") Long postId) {
        log.info("获取帖子评论: postId={}", postId);
        try {
            List<PostCommentVO> comments = postService.getCommentsByPostId(postId);
            return Result.success(comments);
        } catch (BaseException e) {
            log.error("获取帖子评论失败: {}", e.getMessage());
            return Result.error("获取帖子评论失败: " + e.getMessage());
        }
    }


    @GetMapping("/browseHistory")
    @Operation(summary = "获取用户浏览历史", description = "获取用户浏览的帖子历史记录")
    public Result<List<ViewHistory>> getViewHistory() {
        log.info("获取用户浏览历史");
        try {
            List<ViewHistory> history = postService.getViewHistory();
            return Result.success(history);
        } catch (BaseException e) {
            log.error("获取浏览历史失败: {}", e.getMessage());
            return Result.error("获取浏览历史失败: " + e.getMessage());
        }
    }

    @GetMapping("/search")
    @Operation(summary = "搜索帖子", description = "根据关键词搜索帖子")
    public Result<List<PostVO>> searchPosts(@RequestParam("param") String keyword) {
        log.info("搜索帖子: keyword={}", keyword);
        try {
            List<PostVO> posts = postService.searchPosts(keyword);
            return Result.success(posts);
        } catch (BaseException e) {
            log.error("搜索帖子失败: {}", e.getMessage());
            return Result.error("搜索帖子失败: " + e.getMessage());
        }
    }


    @GetMapping("/hotPosts")
    @Operation(summary = "获取热门帖子", description = "获取按浏览量排序的热门帖子")
    public Result<List<PostVO>> getHotPosts() {
        log.info("获取热门帖子");
        try {
            List<PostVO> hotPosts = postService.getHotPosts();
            return Result.success(hotPosts);
        } catch (BaseException e) {
            log.error("获取热门帖子失败: {}", e.getMessage());
            return Result.error("获取热门帖子失败: " + e.getMessage());
        }
    }

    @GetMapping("/hotTags")
    @Operation(summary = "获取热门标签", description = "获取按使用频率排序的热门标签")
    public Result<List<Integer>> getHotTags() {
        log.info("获取热门标签");
        try {
            List<Integer> hotTags = postService.getHotTags();
            return Result.success(hotTags);
        } catch (BaseException e) {
            log.error("获取热门标签失败: {}", e.getMessage());
            return Result.error("获取热门标签失败: " + e.getMessage());
        }
    }

}
