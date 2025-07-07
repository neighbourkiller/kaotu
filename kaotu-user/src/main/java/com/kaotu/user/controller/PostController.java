package com.kaotu.user.controller;

import com.kaotu.base.exception.BaseException;
import com.kaotu.base.model.dto.PostDto;
import com.kaotu.base.model.vo.PostTagVO;
import com.kaotu.base.model.vo.PostVO;
import com.kaotu.base.result.Result;
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
        try{
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
            return Result.success(postDetail);
        } catch (BaseException e) {
            log.error("获取帖子详情失败: {}", e.getMessage());
            return Result.error("获取帖子详情失败: " + e.getMessage());
        }
    }

    @GetMapping("/upvote")
    @Operation(summary = "点赞")
    public Result upvotePost(@RequestParam Long targetId,@RequestParam Boolean targetType) {
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
}
