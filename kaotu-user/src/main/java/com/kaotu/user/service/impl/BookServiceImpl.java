package com.kaotu.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaotu.base.constant.MessageType;
import com.kaotu.base.constant.Status;
import com.kaotu.base.constant.USER;
import com.kaotu.base.constant.WEIGHT;
import com.kaotu.base.context.UserContext;
import com.kaotu.base.exception.BaseException;
import com.kaotu.base.model.dto.RecommendationItemDTO;
import com.kaotu.base.model.dto.RecommendationResponseDTO;
import com.kaotu.base.model.po.*;
import com.kaotu.base.model.vo.BookVO;
import com.kaotu.base.model.vo.CategoryVO;
import com.kaotu.base.model.vo.CommentVO;
import com.kaotu.base.model.vo.CommentVO_;
import com.kaotu.user.mapper.*;
import com.kaotu.user.service.BookService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 书籍信息 服务实现类
 * </p>
 *
 * @author killer
 * @since 2025-07-02
 */
@Slf4j
@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements BookService {

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private FavoriteMapper favoriteMapper;


    @Override
    public BookVO getBookVOById(Integer bookId) {
        log.info("当前用户ID: {}", UserContext.getUserId());
        Book book = bookMapper.selectById(bookId);
        if (book == null) {
            throw new BaseException("书籍不存在");
        }
        String categoryName = bookMapper.getCategoryNameById(book.getSubCategoryId());
        BookVO bookVO = new BookVO();
        BeanUtils.copyProperties(book, bookVO);
        bookVO.setCategoryName(categoryName);
        if (UserContext.getUserId() != null) {
            // 检查当前用户是否已收藏此书籍
            LambdaQueryWrapper<Favorite> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Favorite::getBookId, bookId)
                    .eq(Favorite::getUserId, UserContext.getUserId());
            Favorite favorite = favoriteMapper.selectOne(queryWrapper);
            bookVO.setIsCollected(favorite != null ? 1 : 0); // 1表示已收藏，0表示未收藏
        } else {
            bookVO.setIsCollected(0); // 未登录用户默认未收藏
        }
        return bookVO;
    }

    @Override
    public List<BookVO> searchBooks(String searchParam) {
        if (searchParam == null || searchParam.trim().isEmpty()) {
            throw new BaseException("搜索参数不能为空");
        }
        LambdaQueryWrapper<Book> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Book::getTitle, searchParam)
                .or().like(Book::getPublisher, searchParam);
        List<BookVO> bookVOList = bookMapper.getBookVOList(queryWrapper);
        return bookVOList;
    }

    @Override
    public void collectBook(Integer bookId) {
        if (bookId == null || bookId <= 0) {
            throw new BaseException("无效的书籍ID");
        }
        // 检查当前用户是否已收藏此书籍
        if (favoriteMapper.selectOne(new LambdaQueryWrapper<Favorite>().eq(Favorite::getBookId, bookId)
                .eq(Favorite::getUserId, UserContext.getUserId())) != null) {
            // 取消收藏
            int delete = favoriteMapper.delete(new LambdaQueryWrapper<Favorite>()
                    .eq(Favorite::getBookId, bookId)
                    .eq(Favorite::getUserId, UserContext.getUserId()));
            if (delete <= 0) {
                throw new BaseException("取消收藏书籍失败，请稍后再试");
            } else {
                log.info("用户ID: {} 取消收藏书籍ID: {}", UserContext.getUserId(), bookId);
                return;
            }
        }

        Favorite favorite = new Favorite();
        favorite.setBookId(bookId);
        favorite.setUserId(UserContext.getUserId());
        int insert = favoriteMapper.insert(favorite);
        if (insert <= 0) {
            throw new BaseException("收藏书籍失败，请稍后再试");
        }
    }

    @Override
    public List<BookVO> getBookListByCategoryId(Integer categoryId) {
        if (categoryId == null || categoryId <= 0) {
            throw new BaseException("无效的分类ID");
        }
        LambdaQueryWrapper<Book> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Book::getSubCategoryId, categoryId);
        List<BookVO> bookVOList = bookMapper.getBookVOList(queryWrapper);
        return bookVOList;
    }

    @Override
    public List<CategoryVO> getAllCategories(String userId) {
        log.info("获取所有分类，用户ID: {}", userId);
        List<CategoryVO> categories = bookMapper.getAllCategories(userId);

        return categories;
    }

    @Autowired
    private SystemMessageMapper messageMapper;

    @Override
    @Transactional
    public void addTags(List<Integer> tags) {
        if (tags == null || tags.isEmpty()) {
            throw new BaseException("标签列表不能为空");
        }
        // 清空当前用户的标签
        LambdaQueryWrapper<UserTag> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(UserTag::getUserId, UserContext.getUserId());
        userTagMapper.delete(deleteWrapper);
        // 插入新的标签
        for (Integer tagId : tags) {
            UserTag userTag = new UserTag();
            userTag.setUserId(UserContext.getUserId());
            userTag.setTagId(tagId);
            userTag.setWeight(WEIGHT.TAG_WEIGHT); // 默认权重为NORMAL
            userTagMapper.insert(userTag);
        }

        messageMapper.insertSystemMessage(UserContext.getUserId(),"标签更新成功", "您已成功更新您的标签。",
                String.valueOf(MessageType.OPERATION_SUCCESS),false, LocalDateTime.now());
    }

    @Override
    public List<BookVO> getHotList() {
        LambdaQueryWrapper<Book> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Book::getComments);
        queryWrapper.last("limit 12");
        List<BookVO> books = bookMapper.getBookVOList(queryWrapper);
        return books;
    }

    @Autowired
    private UserTagMapper userTagMapper;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${kaotu.recommend.url}")
    private String baseurl;

    @Override
    public RecommendationResponseDTO getPersonalize() {//伪推荐

/*        log.info("userId: {}", UserContext.getUserId());
        // 获取当前用户的标签
        List<UserTag> userTags = userTagMapper.selectList(new LambdaQueryWrapper<UserTag>().eq(UserTag::getUserId, UserContext.getUserId()));


        List<Integer> tagIds = new ArrayList<>();
        for (UserTag userTag : userTags) {
            tagIds.add(userTag.getTagId());
        }
        if (tagIds.isEmpty()) {
            // 如果没有标签，返回空列表
            return getHotList();
        }
        // 根据标签获取书籍
        LambdaQueryWrapper<Book> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Book::getSubCategoryId, tagIds);
        queryWrapper.orderByDesc(Book::getComments);
        queryWrapper.last("limit 12");
        List<BookVO> personalizedBooks = bookMapper.getBookVOList(queryWrapper);
        return personalizedBooks;*/
        String userId = UserContext.getUserId();
        if (userId == null) {
            log.warn("用户未登录，无法获取个性化推荐，将返回热门列表。");
        }
        String url = baseurl + "?user_id=" + userId;
        try {
            // 发送GET请求，并期望返回 RecommendationResponseDTO 类型的对象
            RecommendationResponseDTO response = restTemplate.getForObject(url, RecommendationResponseDTO.class);

            if (response == null || response.getRecommendations() == null || response.getRecommendations().isEmpty()) {
                log.warn("从推荐服务未获取到有效数据，返回热门列表。");
                List<BookVO> hotList = getHotList();
                RecommendationResponseDTO fallbackResponse = new RecommendationResponseDTO();
                List<RecommendationItemDTO> items=new ArrayList<>();

/*                items.add((RecommendationItemDTO) hotList.stream()
                        .map(bookVO -> {
                                    RecommendationItemDTO item = new RecommendationItemDTO();
                                    BeanUtils.copyProperties(bookVO, item);
                                    return item;
                                }
                            ));
                fallbackResponse.setRecommendations(items);
                return  fallbackResponse;*/

                hotList.forEach(item->{
                    RecommendationItemDTO recommendationItemDTO = new RecommendationItemDTO();
                    BeanUtils.copyProperties(item, recommendationItemDTO);
                    items.add(recommendationItemDTO);
                });
                fallbackResponse.setRecommendations(items);
                return fallbackResponse;


            }
            return response;

        } catch (RestClientException e) {
            log.error("请求个性化推荐服务失败: {}", e.getMessage());
            // 服务调用失败时，可以降级返回热门列表
            List<BookVO> hotList = getHotList();
            RecommendationResponseDTO fallbackResponse = new RecommendationResponseDTO();
            List<RecommendationItemDTO> items=new ArrayList<>();

            hotList.forEach(item->{
                RecommendationItemDTO recommendationItemDTO = new RecommendationItemDTO();
                BeanUtils.copyProperties(item, recommendationItemDTO);
                items.add(recommendationItemDTO);
            });
            fallbackResponse.setRecommendations(items);
            return fallbackResponse;
        }
    }

    @Override
    public List<BookVO> getCollectBooks() {
        // 获取当前用户的收藏书籍ids
        LambdaQueryWrapper<Favorite> favoriteQuery = new LambdaQueryWrapper<>();
        favoriteQuery.eq(Favorite::getUserId, UserContext.getUserId());
        List<Favorite> favorites = favoriteMapper.selectList(favoriteQuery);

        if (favorites.isEmpty()) {
            return Collections.emptyList(); // 如果没有收藏，返回空列表
        }

        List<Integer> bookIds = new ArrayList<>();
        for (Favorite favorite : favorites) {
            bookIds.add(favorite.getBookId());
        }

        QueryWrapper<Book> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("b.id", bookIds);

        return bookMapper.getBookVOList2(queryWrapper);
    }

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private CommentLikeMapper commentLikeMapper;

    @Override
    public List<CommentVO_> getCommentsByBookId(Integer bookId) {
        if (bookId == null || bookId <= 0) {
            throw new BaseException("无效的书籍ID");
        }
        List<Comment> comments = commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getBookId, bookId)
                .eq(Comment::getStatus, Status.ENABLE)); // 只查询状态为1的评论);
        String userId = UserContext.getUserId();
        log.info("获取书籍ID为 {} 的评论，当前用户ID: {}", bookId, userId);
        if (!(userId==null)) // 如果用户ID不为空
            return comments.stream().map(comment -> {
                CommentVO_ commentVO = new CommentVO_();
                BeanUtils.copyProperties(comment, commentVO);
                // 设置书籍标题
                Book book = bookMapper.selectById(comment.getBookId());
                if (book != null) {
                    commentVO.setTitle(book.getTitle());
                }
                // 设置用户名
                User user = userMapper.selectById(comment.getUserId());
                if (user != null) {
                    commentVO.setUsername(user.getUsername());
                } else {
                    commentVO.setUsername(USER.WRITEOFF_USER); // 如果用户不存在，则已注销
                }
                // 查看是否点赞
                if (commentLikeMapper.selectOne(new LambdaQueryWrapper<CommentLike>()
                        .eq(CommentLike::getCommentId, comment.getId())
                        .eq(CommentLike::getUserId, userId)) != null) {
                    commentVO.setIsUpvoted(1);
                } else commentVO.setIsUpvoted(0);

                return commentVO;
            }).collect(Collectors.toList());
        else return comments.stream().map(comment -> {
            CommentVO_ commentVO = new CommentVO_();
            BeanUtils.copyProperties(comment, commentVO);
            // 设置书籍标题
            Book book = bookMapper.selectById(comment.getBookId());
            if (book != null) {
                commentVO.setTitle(book.getTitle());
            }
            // 设置用户名
            User user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                commentVO.setUsername(user.getUsername());
            } else {
                commentVO.setUsername(USER.WRITEOFF_USER); // 如果用户不存在，则已注销
            }
            // 设置点赞状态为0（未点赞）
            commentVO.setIsUpvoted(0);
            return commentVO;
        }).collect(Collectors.toList());
    }
}
