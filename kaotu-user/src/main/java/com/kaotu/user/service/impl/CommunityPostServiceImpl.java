package com.kaotu.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kaotu.base.context.UserContext;
import com.kaotu.base.exception.BaseException;
import com.kaotu.base.model.dto.PostDto;
import com.kaotu.base.model.po.*;
import com.kaotu.base.model.vo.PostTagVO;
import com.kaotu.base.model.vo.PostVO;
import com.kaotu.base.utils.LogUtils;
import com.kaotu.user.mapper.*;
import com.kaotu.user.service.CommunityPostService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 社区帖子表 服务实现类
 * </p>
 *
 * @author killer
 * @since 2025-07-07
 */
@Service
public class CommunityPostServiceImpl extends ServiceImpl<CommunityPostMapper, CommunityPost> implements CommunityPostService {

    @Autowired
    private TagMapper tagMapper;

    @Autowired
    private CommunityPostMapper postMapper;

    @Autowired
    private PostTagMapper postTagMapper;

    @Override
    public List<PostTagVO> getAllTags() {
        return tagMapper.getAllTags();
    }

    @Override
    @Transactional
    public void savePost(PostDto postDto) {
        String userId = UserContext.getUserId();
        if (userId == null) {
            throw new BaseException("用户未登录");
        }
        if (postDto.getContent() == null || postDto.getContent().isEmpty()) {
            throw new BaseException("帖子内容不能为空");
        }
        if (postDto.getTitle() == null || postDto.getTitle().isEmpty()) {
            throw new BaseException("帖子标题不能为空");
        }
        CommunityPost communityPost = new CommunityPost();
        BeanUtils.copyProperties(postDto, communityPost);
        communityPost.setUserId(userId);
        if (postMapper.insert(communityPost) == 0) {
            throw new BaseException("帖子发布失败，请稍后再试");
        }
        // 处理标签关联逻辑
        if (postDto.getTags() != null && !postDto.getTags().isEmpty()) {
            postDto.getTags().forEach(tagId -> {
                if (tagId != null) {
                    PostTag postTag = new PostTag();
                    postTag.setPostId(communityPost.getId());
                    postTag.setTagId(tagId);
                    if (postTagMapper.insert(postTag) == 0) {
                        throw new BaseException("帖子标签关联失败，请稍后再试");
                    }
                }
            });
        }
    }

    @Autowired
    private UserMapper userMapper;

    /**
     * 获取帖子详情
     *
     * @param postId 帖子ID
     * @return PostVO 帖子详情视图对象
     */
    @Override
    public PostVO getPostDetail(Long postId) {
        CommunityPost post = postMapper.selectById(postId);
        if (post == null || !post.getStatus()) {
            throw new BaseException("帖子不存在");
        }
        PostVO postVO = new PostVO();
        BeanUtils.copyProperties(post, postVO);
        // 获取帖子标签
        List<Integer> tagIds = postTagMapper.getTagIdsByPostId(postId);
        postVO.setTagIds(tagIds);
        // 获取帖子作者信息
        User author = userMapper.getByUserId(post.getUserId());
        postVO.setUserName(author.getUsername());
        if (UserContext.getUserId() == null) {
            return postVO;
        }
        // 记录用户浏览帖子日志
        LogUtils.other("用户id: {} 浏览-帖子id: {}", UserContext.getUserId(), postId);

        // 用户是否点赞
        if (likeMapper.selectCount(new LambdaQueryWrapper<UserPostLike>()
                .eq(UserPostLike::getTargetId, postId)
                .eq(UserPostLike::getTargetType, false)
                .eq(UserPostLike::getUserId, UserContext.getUserId())) == 0)
            postVO.setIsLiked(true);
        else postVO.setIsLiked(false);
        // 用户是否收藏
        if (collectionMapper.selectCount(new LambdaQueryWrapper<UserPostCollection>()
                .eq(UserPostCollection::getPostId, postId)
                .eq(UserPostCollection::getUserId, UserContext.getUserId())) == 0)
            postVO.setIsCollected(true);
        else postVO.setIsCollected(false);
        return postVO;
    }


    @Autowired
    private UserPostLikeMapper likeMapper;
    @Autowired
    private PostCommentMapper commentMapper;

    @Override
    @Transactional
    public void upvotePost(Long targetId, Boolean targetType) {
        Integer i = likeMapper.selectCount(new LambdaQueryWrapper<UserPostLike>().eq(UserPostLike::getTargetId, targetId)
                .eq(UserPostLike::getTargetType, targetType)
                .eq(UserPostLike::getUserId, UserContext.getUserId()));
        if (i == 0) {
            UserPostLike userPostLike = new UserPostLike();
            userPostLike.setTargetId(targetId);
            userPostLike.setTargetType(targetType);
            userPostLike.setUserId(UserContext.getUserId());
            if (likeMapper.insert(userPostLike) == 0) {
                throw new BaseException("点赞失败，请稍后再试");
            }
            // 更新帖子点赞数
            if (!targetType) {
                // 帖子点赞
                if (postMapper.updateLikeCount(targetId, 1) == 0) {
                    throw new BaseException("帖子点赞数更新失败，请稍后再试");
                }
            } else {
                // 评论点赞
                if (commentMapper.updateLikeCount(targetId, 1) == 0) {
                    throw new BaseException("评论点赞数更新失败，请稍后再试");
                }
            }
        } else {
            // 已经点赞，取消点赞
            if (likeMapper.delete(new LambdaQueryWrapper<UserPostLike>().eq(UserPostLike::getTargetId, targetId)
                    .eq(UserPostLike::getTargetType, targetType)
                    .eq(UserPostLike::getUserId, UserContext.getUserId())) == 0) {
                throw new BaseException("取消点赞失败，请稍后再试");
            }
            // 更新帖子点赞数
            if (!targetType) {
                // 帖子点赞
                if (postMapper.updateLikeCount(targetId, -1) == 0) {
                    throw new BaseException("帖子点赞数更新失败，请稍后再试");
                }
            } else {
                // 评论点赞
                if (commentMapper.updateLikeCount(targetId, -1) == 0) {
                    throw new BaseException("评论点赞数更新失败，请稍后再试");
                }
            }
        }
    }

    @Autowired
    private UserPostCollectionMapper collectionMapper;

    @Override
    @Transactional
    public void collectPost(Long postId) {
        Integer i = collectionMapper.selectCount(new LambdaQueryWrapper<UserPostCollection>()
                .eq(UserPostCollection::getPostId, postId)
                .eq(UserPostCollection::getUserId, UserContext.getUserId()));
        if (i == 0) {
            UserPostCollection userPostCollection = new UserPostCollection();
            userPostCollection.setPostId(postId);
            userPostCollection.setUserId(UserContext.getUserId());
            if (collectionMapper.insert(userPostCollection) == 0) {
                throw new BaseException("帖子收藏失败，请稍后再试");
            }
            // 更新帖子收藏数
            if (postMapper.updateCollectCount(postId, 1) == 0) {
                throw new BaseException("帖子收藏数更新失败，请稍后再试");
            }
        } else {
            // 已经收藏，取消收藏
            if (collectionMapper.delete(new LambdaQueryWrapper<UserPostCollection>()
                    .eq(UserPostCollection::getPostId, postId)
                    .eq(UserPostCollection::getUserId, UserContext.getUserId())) == 0) {
                throw new BaseException("取消收藏失败，请稍后再试");
            }
            // 更新帖子收藏数
            if (postMapper.updateCollectCount(postId, -1) == 0) {
                throw new BaseException("帖子收藏数更新失败，请稍后再试");
            }
        }
    }
}
