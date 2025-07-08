package com.kaotu.user.service;

import com.kaotu.base.model.dto.PostCommentDto;
import com.kaotu.base.model.dto.PostDto;
import com.kaotu.base.model.po.CommunityPost;
import com.baomidou.mybatisplus.extension.service.IService;
import com.kaotu.base.model.vo.PostTagVO;
import com.kaotu.base.model.vo.PostVO;

import java.util.List;

/**
 * <p>
 * 社区帖子表 服务类
 * </p>
 *
 * @author killer
 * @since 2025-07-07
 */
public interface CommunityPostService extends IService<CommunityPost> {

    /**
     * 获取所有帖子标签
     *
     * @return List<PostTagVO>
     */
    List<PostTagVO> getAllTags();

    void savePost(PostDto postDto);

    /**
     * 获取帖子详情
     * @param postId
     * @return com.kaotu.base.model.vo.PostVO
     */
    PostVO getPostDetail(Long postId);

    /**
     * 点赞或取消点赞帖子
     * @param targetId
     * @param targetType
     */
    void upvotePost(Long targetId,Boolean targetType);

    /**
     * 收藏或取消收藏帖子
     * @param postId
     */
    void collectPost(Long postId);

    /**
     * 获取用户收藏的帖子列表
     * @return List<PostVO>
     */
    List<PostVO> getCollectedPosts();

    /**
     * 评论帖子
     * @param commentDto
     */
    void commentPost(PostCommentDto commentDto);

    List<PostVO> getRecommendedPosts();

    void removePostById(Long postId);

    void removeCommentById(Long commentId);

    void recordPostView(Long postId, Integer time);
}
