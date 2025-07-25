package com.kaotu.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaotu.base.constant.MessageType;
import com.kaotu.base.constant.Status;
import com.kaotu.base.context.UserContext;
import com.kaotu.base.exception.BaseException;
import com.kaotu.base.model.dto.CommentDto;
import com.kaotu.base.model.po.*;
import com.kaotu.base.model.vo.CommentVO;
import com.kaotu.base.model.vo.CommentVO_;
import com.kaotu.base.properties.JwtProperties;
import com.kaotu.base.utils.JwtUtil;
import com.kaotu.base.utils.LogUtils;
import com.kaotu.base.utils.PasswordUtil;
import com.kaotu.user.mapper.*;
import com.kaotu.user.service.UserService;
import io.netty.util.internal.UnstableApi;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtProperties jwtProperties;

    @Override
    @Transactional
    public String register(User user) {
        User selected = userMapper.selectById(user.getUserId());
        if (selected != null)
            throw new BaseException("账号已存在");
        if (!PasswordUtil.isUserIdValid(user.getUserId()))
            throw new BaseException("账号格式不正确");
        if (!PasswordUtil.isValid(user.getPassword()))
            throw new BaseException("密码格式不正确");

        user.setUsername("用户" + user.getUserId());
        //密码加密逻辑
        String salt = UUID.randomUUID().toString();
        user.setSalt(salt);
        user.setPassword(hashPassword(user.getPassword(), salt));

        int insert = userMapper.insert(user);
        if (insert == 0)
            throw new BaseException("系统错误");
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getUserId());

        messageMapper.insertSystemMessage(user.getUserId(), "注册成功", "欢迎使用考图，您的账号已注册成功！", String.valueOf(MessageType.OPERATION_SUCCESS), false, LocalDateTime.now());
        return JwtUtil.createJWT(jwtProperties.getSecretKey(), jwtProperties.getTtl(), claims);

    }

    /**
     * 用户登录
     *
     * @param user 包含用户ID和本次输入的密码
     * @return 登录成功后返回JWT
     */
    public String login(User user) {
        // 1. 根据userId查询数据库中的用户信息
//        User userInDb = userMapper.getByUserId(user.getUserId());
        log.info("userId: {}", user.getUserId());
        User userInDb = userMapper.selectById(user.getUserId());
        // 2. 判断用户是否存在
        if (userInDb == null) {
            throw new BaseException("账号或密码错误");
        }

        // 3. 对用户输入的密码进行加密，使用从数据库中取出的盐
        String hashedPassword = hashPassword(user.getPassword(), userInDb.getSalt());

        // 4. 将加密后的密码与数据库中的密码进行比对
        if (!hashedPassword.equals(userInDb.getPassword())) {
            throw new BaseException("账号或密码错误");
        }

        userInDb.setLastLogin(LocalDateTime.now());
        int update = userMapper.updateById(userInDb);
        if (update == 0) {
            throw new BaseException("系统错误，登录失败");
        }

        // 5. 密码正确，生成token并返回
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userInDb.getUserId());
        return JwtUtil.createJWT(jwtProperties.getSecretKey(), jwtProperties.getTtl(), claims);
    }

    public void modifyEmail(String userId, String email) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BaseException("用户不存在");
        }
        user.setEmail(email);
        int update = userMapper.updateById(user);
        if (update == 0) {
            throw new BaseException("系统错误，修改邮箱失败");
        }
    }

    /**
     * 使用SHA-256对密码进行哈希处理
     *
     * @param password 原始密码
     * @param salt     盐值
     * @return 哈希后的密码
     */
    private String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            // 将密码和盐结合
            String text = password + salt;
            final byte[] hashbytes = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            // 将字节数组转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte hashbyte : hashbytes) {
                String hex = Integer.toHexString(0xff & hashbyte);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            log.error("密码加密失败", e);
            throw new BaseException("系统错误，无法加密密码");
        }
    }

    private static final Logger logger = LoggerFactory.getLogger("browse");

    @Override
    @Transactional
    public void modifyUsername(String userId, String username) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BaseException("用户不存在");
        }
        if (username == null || username.isEmpty()) {
            throw new BaseException("用户名不能为空");
        }
        // TODO 审核用户名

        user.setUsername(username);
        int update = userMapper.updateById(user);
        if (update == 0) {
            throw new BaseException("系统错误，修改用户名失败");
        }

        messageMapper.insertSystemMessage(userId, "用户名修改成功", "您的用户名已被修改为: " + username,
                String.valueOf(MessageType.OPERATION_SUCCESS), false, LocalDateTime.now());
    }

    @Autowired
    private BookBrowseHistoryMapper historyMapper;

    @Override
    @Transactional
    public void recordBrowseTime(int bookId, int timeInSeconds) {
        // 记录用户浏览书籍的时间逻辑
        // 这里可以实现具体的业务逻辑，比如将浏览时间存储到数据库中
//        log.info("用户ID: {}, 浏览书籍ID: {}, 停留时间: {}秒", UserContext.getUserId(), bookId, timeInSeconds);

        logger.info("用户ID: {}, 书籍ID: {}, 停留时间: {}秒", UserContext.getUserId(), bookId, timeInSeconds);
        // 检查用户是否已经浏览过该书籍
        LambdaQueryWrapper<BookBrowseHistory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BookBrowseHistory::getUserId, UserContext.getUserId())
                .eq(BookBrowseHistory::getBookId, bookId);
        BookBrowseHistory history = historyMapper.selectOne(queryWrapper);
        if (history == null) {
            // 如果没有浏览记录，则创建新的记录
            history = new BookBrowseHistory();
            history.setUserId(UserContext.getUserId());
            history.setBookId(bookId);
            history.setBrowseTime(LocalDateTime.now());
            history.setDuration(timeInSeconds);
            int insert = historyMapper.insert(history);
            if (insert == 0) {
                throw new BaseException("记录浏览时间失败，系统错误");
            }
        } else {
            // 如果已经有浏览记录，则更新停留时间
            history.setDuration(history.getDuration() + timeInSeconds);
            int update = historyMapper.updateById(history);
            if (update == 0) {
                throw new BaseException("更新浏览时间失败，系统错误");
            }
        }
    }

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private BookMapper bookMapper;

    @Override
    public void commentBook(CommentDto commentDto) {

        log.info("用户ID: {}, 评论书籍ID: {}, 评论内容: {}", UserContext.getUserId(), commentDto.getBookId(), commentDto.getContent());
        if (!commentDto.getUserId().equals(UserContext.getUserId())) {
            log.error("评论失败，用户ID不匹配: 用户ID={}, 当前登录用户ID={}", commentDto.getUserId(), UserContext.getUserId());
            throw new BaseException("评论失败，用户ID不匹配");
        }
        if (commentDto.getStar() < 1 || commentDto.getStar() > 5) {
            throw new BaseException("评论失败，星级必须在1到5之间");
        }
        if (commentDto.getContent() == null) {
            throw new BaseException("评论内容不能为空");
        }
        LocalDateTime ub = userMapper.getByUserId(UserContext.getUserId()).getUnblockTime();
        if (ub != null && ub.isAfter(LocalDateTime.now())) {
            throw new BaseException("账号已被封禁，无法评论");
        }
        Comment comment = new Comment();
        BeanUtils.copyProperties(commentDto, comment);
        comment.setCommentTime(LocalDateTime.now());
        log.info(comment.toString());
        commentMapper.insert(comment);

        LogUtils.browse("用户ID: {}, 评论书籍ID: {}, 评论内容: {}", UserContext.getUserId(), commentDto.getBookId(), commentDto.getContent());
    }

    /**
     * 获取当前用户的评论列表
     *
     * @return List<CommentVO> 包含评论信息的视图对象列表
     */
    @Override
    public List<CommentVO_> myComments() {
        // 搜索评论
        LambdaQueryWrapper<Comment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Comment::getUserId, UserContext.getUserId())
                .eq(Comment::getStatus, Status.ENABLE)
                .orderByDesc(Comment::getCommentTime);
        List<Comment> comments = commentMapper.selectList(queryWrapper);
        if (comments.isEmpty())
            return null;
        User user = userMapper.selectById(UserContext.getUserId());
        String username = user.getUsername();
        return comments.stream().map(comment -> {
            CommentVO_ commentVO = new CommentVO_();
            BeanUtils.copyProperties(comment, commentVO);
            Book book = bookMapper.selectById(comment.getBookId());
            if (book != null) {
                commentVO.setTitle(book.getTitle());
            }
            // 设置用户名
            commentVO.setUsername(username);
            // 设置是否点赞评论
            if (commentLikeMapper.selectOne(new LambdaQueryWrapper<CommentLike>()
                    .eq(CommentLike::getCommentId, comment.getId())
                    .eq(CommentLike::getUserId, UserContext.getUserId())) == null)
                commentVO.setIsUpvoted(0);
            else commentVO.setIsUpvoted(1);

            return commentVO;
        }).collect(Collectors.toList());

    }

    @Autowired
    private CommentLikeMapper commentLikeMapper;

    @Override
    @Transactional
    public void upvoteComment(Long commentId) {

        // 检查是否已经点赞
        CommentLike one = commentLikeMapper.selectOne(new LambdaQueryWrapper<CommentLike>()
                .eq(CommentLike::getUserId, UserContext.getUserId())
                .eq(CommentLike::getCommentId, commentId));
        if (one != null) {
            log.info("已点赞，则取消点赞");
            int delete = commentLikeMapper.deleteById(one.getId());
            if (delete == 0) {
                throw new BaseException("取消点赞失败，系统错误");
            }
            int i = commentMapper.updateCommentUps(commentId, -1);
            if (i == 0) {
                throw new BaseException("取消点赞失败，系统错误");
            }
            return;
        }
        // 插入点赞记录
        CommentLike commentLike = new CommentLike();
        commentLike.setCommentId(commentId);
        commentLike.setUserId(UserContext.getUserId());
        commentLike.setCreateTime(LocalDateTime.now());
        int insert = commentLikeMapper.insert(commentLike);
        if (insert == 0) {
            throw new BaseException("点赞失败，系统错误");
        }
        // 更新评论的点赞数
        int i = commentMapper.updateCommentUps(commentId, 1);
        if (i == 0) {
            throw new BaseException("点赞失败，系统错误");
        }
        LogUtils.browse("用户ID: {}, 点赞评论ID: {}", UserContext.getUserId(), commentId);
    }

    @Autowired
    private SystemMessageMapper messageMapper;

    @Override
    public List<SystemMessage> getSystemMessages(String userId) {
        if (userId == null)
            return null;
        List<SystemMessage> messages = messageMapper.selectList(new LambdaQueryWrapper<SystemMessage>()
                .eq(SystemMessage::getReceiverId, userId)
                .orderByDesc(SystemMessage::getCreateTime));
        if (messages.isEmpty()) {
            return null;
        }
        return messages;
    }

    @Override
    @Transactional
    public void markMessagesAsRead(List<Long> messageIds) {

        if (messageIds == null || messageIds.isEmpty()) {
            throw new BaseException("消息ID列表不能为空");
        }
        // 更新消息状态为已读
        for (Long messageId : messageIds) {
            messageMapper.updateMessageAsRead(messageId, UserContext.getUserId(), true);
        }
    }

    @Autowired
    private FavoriteMapper favoriteMapper;
    @Autowired
    private CommunityPostMapper postMapper;
    @Autowired
    private PostCommentMapper postCommentMapper;
    @Autowired
    private UserTagMapper userTagMapper;
    @Autowired
    private UserPostLikeMapper userPostLikeMapper;
    @Autowired
    private UserPostCollectionMapper collectionMapper;
    @Autowired
    private PostViewHistoryMapper postHistoryMapper;

    /**
     * 注销用户账号
     *
     * @param userId
     */
    @Override
    @Transactional
    public void writeOff(String userId) {
        userMapper.deleteById(userId);
        // 删除用户的评论
        commentMapper.delete(new LambdaQueryWrapper<Comment>().eq(Comment::getUserId, userId));
        // 删除用户的浏览记录
        historyMapper.delete(new LambdaQueryWrapper<BookBrowseHistory>().eq(BookBrowseHistory::getUserId, userId));
        // 删除用户的点赞记录
        commentLikeMapper.delete(new LambdaQueryWrapper<CommentLike>().eq(CommentLike::getUserId, userId));
        // 删除用户的系统消息
        messageMapper.delete(new LambdaQueryWrapper<SystemMessage>().eq(SystemMessage::getReceiverId, userId));
        // 删除用户的收藏记录
        favoriteMapper.delete(new LambdaQueryWrapper<Favorite>().eq(Favorite::getUserId, userId));
        // 删除用户的社区帖子
        postMapper.delete(new LambdaQueryWrapper<CommunityPost>().eq(CommunityPost::getUserId, userId));
        // 删除用户的社区评论
        postCommentMapper.delete(new LambdaQueryWrapper<PostComment>().eq(PostComment::getUserId, userId));
        // 删除用户的标签
        userTagMapper.delete(new LambdaQueryWrapper<UserTag>().eq(UserTag::getUserId, userId));
        // 删除用户的帖子点赞记录
        userPostLikeMapper.delete(new LambdaQueryWrapper<UserPostLike>().eq(UserPostLike::getUserId, userId));
        // 删除用户的帖子收藏记录
        collectionMapper.delete(new LambdaQueryWrapper<UserPostCollection>().eq(UserPostCollection::getUserId, userId));
        // 删除用户的帖子浏览记录
        postHistoryMapper.delete(new LambdaQueryWrapper<PostViewHistory>().eq(PostViewHistory::getUserId, userId));
    }
}
