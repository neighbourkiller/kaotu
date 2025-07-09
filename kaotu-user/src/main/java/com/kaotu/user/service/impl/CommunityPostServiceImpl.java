package com.kaotu.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.kaotu.base.context.UserContext;
import com.kaotu.base.exception.BaseException;
import com.kaotu.base.model.dto.PostCommentDto;
import com.kaotu.base.model.dto.PostDto;
import com.kaotu.base.model.po.*;
import com.kaotu.base.model.vo.PostCommentVO;
import com.kaotu.base.model.vo.PostTagVO;
import com.kaotu.base.model.vo.PostVO;
import com.kaotu.base.model.vo.ViewHistory;
import com.kaotu.base.utils.LogUtils;
import com.kaotu.user.mapper.*;
import com.kaotu.user.service.CommunityPostService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaotu.user.util.PostPopularityAnalyzer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * 社区帖子表 服务实现类
 * </p>
 *
 * @author killer
 * @since 2025-07-07
 */
@Slf4j
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
        postVO.setUsername(author.getUsername());
        if (UserContext.getUserId() == null) {
            log.info("获取帖子详情时用户未登录，postId: {}", postId);
            postVO.setIsLiked(false);
            postVO.setIsCollected(false);
            return postVO;
        }

        // 用户是否点赞
        postVO.setIsLiked(likeMapper.selectCount(new LambdaQueryWrapper<UserPostLike>()
                .eq(UserPostLike::getTargetId, postId)
                .eq(UserPostLike::getTargetType, false)
                .eq(UserPostLike::getUserId, UserContext.getUserId())) > 0);
        // 用户是否收藏
        postVO.setIsCollected(collectionMapper.selectCount(new LambdaQueryWrapper<UserPostCollection>()
                .eq(UserPostCollection::getPostId, postId)
                .eq(UserPostCollection::getUserId, UserContext.getUserId())) > 0);
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
                LogUtils.other("用户id: {} 点赞-帖子id: {}", UserContext.getUserId(), targetId);
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
                LogUtils.other("用户id: {} 取消点赞-帖子id: {}", UserContext.getUserId(), targetId);
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
            LogUtils.other("用户id: {} 收藏-帖子id: {}", UserContext.getUserId(), postId);
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
            LogUtils.other("用户id: {} 取消收藏-帖子id: {}", UserContext.getUserId(), postId);
        }
    }

    @Override
    public List<PostVO> getCollectedPosts() {
        List<Long> postIds = collectionMapper.selectList(new LambdaQueryWrapper<UserPostCollection>()
                .eq(UserPostCollection::getUserId, UserContext.getUserId()))
                .stream()
                .map(UserPostCollection::getPostId).collect(Collectors.toList());
        List<PostVO> postVOs=new ArrayList<>();
//        String username= userMapper.getByUserId(UserContext.getUserId()).getUsername();
        if (!postIds.isEmpty()) {
            for(Long postId:postIds){
                CommunityPost post = postMapper.selectById(postId);
                PostVO postVO=new PostVO();
                BeanUtils.copyProperties(post,postVO);
                postVO.setTagIds(postTagMapper.getTagIdsByPostId(postId));
                postVO.setUsername(userMapper.getByUserId(post.getUserId()).getUsername());
                postVO.setIsCollected(true);
                postVOs.add(postVO);
            }
            return postVOs;
        }
        return Collections.emptyList();
    }

    @Override
    @Transactional
    public void commentPost(PostCommentDto commentDto) {
        if (commentDto.getContent() == null || commentDto.getContent().isEmpty()) {
            throw new BaseException("评论内容不能为空");
        }
        if (commentDto.getPostId() == null) {
            throw new BaseException("帖子ID不能为空");
        }
        // 检查帖子是否存在
        CommunityPost post = postMapper.selectById(commentDto.getPostId());
        if (post == null || !post.getStatus()) {
            throw new BaseException("帖子不存在或已被删除");
        }
        // 创建评论对象
        PostComment postComment = new PostComment();
        BeanUtils.copyProperties(commentDto, postComment);
        postComment.setUserId(UserContext.getUserId());
        if (commentMapper.insert(postComment) == 0) {
            throw new BaseException("评论失败，请稍后再试");
        }
        // 更新帖子评论数
        if (postMapper.updateCommentCount(commentDto.getPostId(), 1) == 0) {
            throw new BaseException("帖子评论数更新失败，请稍后再试");
        }
    }

    @Override
    public List<PostVO> getNewestPosts() {
        String currentUserId = UserContext.getUserId();
        List<CommunityPost> finalPosts = new ArrayList<>();

        if (currentUserId == null) {
            // 用户未登录，直接获取最新的10个帖子
            finalPosts = postMapper.selectList(new LambdaQueryWrapper<CommunityPost>()
                    .eq(CommunityPost::getStatus, true)
                    .orderByDesc(CommunityPost::getCreateTime)
                    .last("LIMIT 10"));
        } else {
            // 用户已登录，需要排除已浏览的帖子
            // 1. 获取用户浏览过的帖子ID列表
            List<Long> viewedPostIds = historyMapper.selectList(new LambdaQueryWrapper<PostViewHistory>()
                            .eq(PostViewHistory::getUserId, currentUserId))
                    .stream()
                    .map(PostViewHistory::getPostId)
                    .collect(Collectors.toList());

            // 2. 获取用户未浏览过的最新帖子
            List<CommunityPost> unseenPosts;
            LambdaQueryWrapper<CommunityPost> unseenQuery = new LambdaQueryWrapper<CommunityPost>()
                    .eq(CommunityPost::getStatus, true)
                    .orderByDesc(CommunityPost::getCreateTime)
                    .last("LIMIT 10");

            if (!viewedPostIds.isEmpty()) {
                unseenQuery.notIn(CommunityPost::getId, viewedPostIds);
            }
            unseenPosts = postMapper.selectList(unseenQuery);
            finalPosts.addAll(unseenPosts);

            // 3. 如果未浏览的帖子不足10个，用已浏览的帖子补充
            int needed = 10 - finalPosts.size();
            if (needed > 0 && !viewedPostIds.isEmpty()) {
                List<CommunityPost> seenPosts = postMapper.selectList(new LambdaQueryWrapper<CommunityPost>()
                        .eq(CommunityPost::getStatus, true)
                        .in(CommunityPost::getId, viewedPostIds)
                        .orderByDesc(CommunityPost::getCreateTime) // 按最新浏览排序也可以，这里按创建时间
                        .last("LIMIT " + needed));
                finalPosts.addAll(seenPosts);
            }
        }

        if (finalPosts.isEmpty()) {
            return Collections.emptyList();
        }

        // 4. 将CommunityPost列表转换为PostVO列表，并填充额外信息
        return finalPosts.stream().map(post -> {
            PostVO postVO = new PostVO();
            BeanUtils.copyProperties(post, postVO);

            // 填充作者信息
            User author = userMapper.getByUserId(post.getUserId());
            if (author != null) {
                postVO.setUsername(author.getUsername());
            }

            // 填充标签信息
            postVO.setTagIds(postTagMapper.getTagIdsByPostId(post.getId()));

            // 如果用户登录，填充点赞和收藏状态
            if (currentUserId != null) {
                postVO.setIsLiked(likeMapper.selectCount(new LambdaQueryWrapper<UserPostLike>()
                        .eq(UserPostLike::getUserId, currentUserId)
                        .eq(UserPostLike::getTargetType, false) // 帖子类型
                        .eq(UserPostLike::getTargetId, post.getId())) > 0);

                postVO.setIsCollected(collectionMapper.selectCount(new LambdaQueryWrapper<UserPostCollection>()
                        .eq(UserPostCollection::getUserId, currentUserId)
                        .eq(UserPostCollection::getPostId, post.getId())) > 0);
            } else {
                postVO.setIsLiked(false);
                postVO.setIsCollected(false);
            }

            return postVO;
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void removePostById(Long postId) {
        CommunityPost post = postMapper.selectById(postId);
        if(post == null || !post.getStatus()) {
            throw new BaseException("删除失败");
        }
        if(!post.getUserId().equals(UserContext.getUserId())) {
            throw new BaseException("删除失败");
        }
        post.setStatus(false);
        if(postMapper.updateById(post)== 0) {
            throw new BaseException("删除失败");
        }
        // 更新评论状态
        commentMapper.updateCommentStatus(postId, false);
    }

    @Override
    @Transactional
    public void removeCommentById(Long commentId) {
        PostComment comment = commentMapper.selectById(commentId);
        if (comment == null || !comment.getStatus()) {
            throw new BaseException("删除失败");
        }
        if (!comment.getUserId().equals(UserContext.getUserId())) {
            throw new BaseException("删除失败");
        }
        comment.setStatus(false);
        if (commentMapper.updateById(comment) == 0) {
            throw new BaseException("删除失败");
        }
        // 更新帖子评论数
        if (postMapper.updateCommentCount(comment.getPostId(), -1) == 0) {
            throw new BaseException("删除失败");
        }
        // 更新子评论状态
        invalidateSubComments(commentId);

    }
    /**
     * 递归禁用所有子评论
     *
     * @param parentId 父评论ID
     */
    private void invalidateSubComments(Long parentId) {
        // 查找所有直接子评论
        LambdaQueryWrapper<PostComment> wrapper = new LambdaQueryWrapper<PostComment>()
                .eq(PostComment::getParentId, parentId)
                .eq(PostComment::getStatus, true); // 只处理状态为true的，避免重复处理
        List<PostComment> children = commentMapper.selectList(wrapper);

        if (children == null || children.isEmpty()) {
            return; // 递归的终止条件：没有子评论了
        }

        // 获取所有子评论的ID
        List<Long> childIds = children.stream().map(PostComment::getId).collect(Collectors.toList());

        // 批量更新子评论的状态为false
        PostComment updateEntity = new PostComment();
        updateEntity.setStatus(false);
        if(commentMapper.update(updateEntity, new LambdaQueryWrapper<PostComment>().in(PostComment::getId, childIds))< childIds.size()) {
            throw new BaseException("删除子评论失败，请稍后再试");
        }
        // 对每一个子评论，递归调用此方法，以禁用它们的子评论
        for (Long childId : childIds) {
            invalidateSubComments(childId);
        }
    }

    @Autowired
    private PostViewHistoryMapper historyMapper;

    @Override
    public void recordPostView(Long postId, Integer time) {
        if (postId == null || time == null || time <= 0) {
            throw new BaseException("无效的帖子ID或浏览时间");
        }
        // 记录浏览日志
        if(time>=20)
            LogUtils.other("用户id: {} 浏览-帖子id: {}, 浏览时长: {}秒", UserContext.getUserId(), postId, time);
        // 检查是否已经记录过浏览历史
        PostViewHistory history = historyMapper.selectOne(new LambdaQueryWrapper<PostViewHistory>()
                .eq(PostViewHistory::getPostId, postId)
                .eq(PostViewHistory::getUserId, UserContext.getUserId()));
        if (history != null) {
            if((historyMapper.updateViewTime(history.getId(),LocalDateTime.now(), time)==0)){
                throw new BaseException("更新浏览记录失败，请稍后再试");
            }
        }
        else {
            // 创建新的浏览记录
            PostViewHistory newHistory = new PostViewHistory();
            newHistory.setPostId(postId);
            newHistory.setUserId(UserContext.getUserId());
            newHistory.setViewTime(LocalDateTime.now());
            newHistory.setTimeLength(time);
            if (historyMapper.insert(newHistory) == 0) {
                throw new BaseException("记录浏览历史失败，请稍后再试");
            }
        }
        // 更新帖子浏览数
        if (postMapper.updateViewCount(postId, 1) == 0) {
            throw new BaseException("帖子浏览数更新失败，请稍后再试");
        }
    }



    @Override
    public List<PostCommentVO> getCommentsByPostId(Long postId) {
        // 1. 查询出指定帖子下所有状态为正常的评论
        LambdaQueryWrapper<PostComment> queryWrapper = new LambdaQueryWrapper<PostComment>()
                .eq(PostComment::getPostId, postId)
                .eq(PostComment::getStatus, true)
                .orderByAsc(PostComment::getCreateTime); // 按时间升序
        List<PostComment> allComments = commentMapper.selectList(queryWrapper);

        if (allComments.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. 如果用户已登录，查询这些评论中哪些被用户点赞了
        String currentUserId = UserContext.getUserId();
        // 使用Set以O(1)的复杂度检查是否点赞
        final java.util.Set<Long> likedCommentIds = new java.util.HashSet<>();
        if (currentUserId != null) {
            List<Long> commentIds = allComments.stream().map(PostComment::getId).collect(Collectors.toList());
            if (!commentIds.isEmpty()) {
                List<UserPostLike> likes = likeMapper.selectList(new LambdaQueryWrapper<UserPostLike>()
                        .eq(UserPostLike::getUserId, currentUserId)
                        .eq(UserPostLike::getTargetType, true) // true表示评论
                        .in(UserPostLike::getTargetId, commentIds));
                // 将点赞过的评论ID存入Set
                likes.forEach(like -> likedCommentIds.add(like.getTargetId()));
            }
        }

        // 3. 将所有评论转换为VO，并设置isLiked状态
        List<PostCommentVO> allCommentVOs = allComments.stream().map(comment -> {
            PostCommentVO vo = new PostCommentVO();
            BeanUtils.copyProperties(comment, vo);
            // 查询并设置用户信息
            User user = userMapper.getByUserId(comment.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
            }
            // 根据查询结果设置isLiked状态
            vo.setIsLiked(likedCommentIds.contains(comment.getId()));
            return vo;
        }).collect(Collectors.toList());

        // 4. 构建树形结构
        Map<Long, PostCommentVO> commentMap = allCommentVOs.stream()
                .collect(Collectors.toMap(PostCommentVO::getId, vo -> vo));

        List<PostCommentVO> rootComments = new ArrayList<>();
        for (PostCommentVO vo : allCommentVOs) {
            Long parentId = vo.getParentId();
            if (parentId != null && commentMap.containsKey(parentId)) {
                PostCommentVO parentVO = commentMap.get(parentId);
                if (parentVO.getChildren() == null) {
                    parentVO.setChildren(new ArrayList<>());
                }
                parentVO.getChildren().add(vo);
            } else {
                rootComments.add(vo);
            }
        }

        return rootComments;
    }

    @Override
    public List<ViewHistory> getViewHistory() {
        List<PostViewHistory> list = historyMapper.selectList(new LambdaQueryWrapper<PostViewHistory>()
                .eq(PostViewHistory::getUserId, UserContext.getUserId()));
        if (list != null && !list.isEmpty()) {
            return list.stream().map(history -> {
                ViewHistory viewHistory = new ViewHistory();
                BeanUtils.copyProperties(history, viewHistory);
                // 设置帖子标题
                CommunityPost post = postMapper.selectById(history.getPostId());
                if (post != null) {
                    viewHistory.setTitle(post.getTitle());
                    viewHistory.setContent(post.getContent());
                    viewHistory.setUserId(post.getUserId());// 发帖人ID
                    viewHistory.setUsername(userMapper.getByUserId(post.getUserId()).getUsername()); // 发帖人用户名
                }
                return viewHistory;
            }).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public List<PostVO> searchPosts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 查找用户名匹配关键字的用户ID
        List<String> userIds = userMapper.selectList(new LambdaQueryWrapper<User>()
                        .like(User::getUsername, keyword))
                .stream()
                .map(User::getUserId)
                .collect(Collectors.toList());

        // 2. 构建帖子查询条件
        LambdaQueryWrapper<CommunityPost> queryWrapper = new LambdaQueryWrapper<CommunityPost>()
                .eq(CommunityPost::getStatus, true);

        // 组合查询：(标题 like keyword) OR (内容 like keyword) OR (用户ID in [...])
        queryWrapper.and(wrapper -> {
            wrapper.like(CommunityPost::getTitle, keyword)
                    .or()
                    .like(CommunityPost::getContent, keyword);
            if (!userIds.isEmpty()) {
                wrapper.or().in(CommunityPost::getUserId, userIds);
            }
        });

        queryWrapper.orderByDesc(CommunityPost::getCreateTime);

        List<CommunityPost> posts = postMapper.selectList(queryWrapper);

        if (posts.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 转换为PostVO
        return posts.stream().map(post -> {
            PostVO postVO = new PostVO();
            BeanUtils.copyProperties(post, postVO);
            postVO.setTagIds(postTagMapper.getTagIdsByPostId(post.getId()));
            User author = userMapper.getByUserId(post.getUserId());
            if (author != null) {
                postVO.setUsername(author.getUsername());
            }
            return postVO;
        }).collect(Collectors.toList());
    }

    @Autowired
    private PostPopularityAnalyzer analyzer;

    @Override
    public List<PostVO> getHotPosts() {
        List<Long> posts = analyzer.getHotPosts();
        List<PostVO> hotPosts = new ArrayList<>();
        for(Long postId:posts){
            CommunityPost post = postMapper.selectById(postId);
            if(post==null || !post.getStatus()){
                continue;
            }
            PostVO postVO=new PostVO();
            BeanUtils.copyProperties(post,postVO);
            postVO.setTagIds(postTagMapper.getTagIdsByPostId(postId));
            postVO.setUsername(userMapper.getByUserId(post.getUserId()).getUsername());
            hotPosts.add(postVO);
        }

        return hotPosts;
    }

    @Override
    public List<Integer> getHotTags() {
        // 获取热门帖子的标签
        List<Long> postIds = analyzer.getHotPosts();
        List<Integer> hotTags = new ArrayList<>();
        for(Long postId : postIds) {
            List<Integer> tagIds = postTagMapper.getTagIdsByPostId(postId);
            if (tagIds != null && !tagIds.isEmpty()) {
                hotTags.addAll(tagIds);
            }
        }
        if(hotTags.size()>6){
            // 如果超过6个，取前6个
            hotTags = hotTags.stream().distinct().limit(6).collect(Collectors.toList());
        } else {
            // 去重
            hotTags = hotTags.stream().distinct().collect(Collectors.toList());
        }
        return hotTags;
    }

}
