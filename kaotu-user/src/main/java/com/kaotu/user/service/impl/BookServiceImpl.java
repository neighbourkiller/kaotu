package com.kaotu.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.segments.MergeSegments;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaotu.base.constant.WEIGHT;
import com.kaotu.base.context.UserContext;
import com.kaotu.base.exception.BaseException;
import com.kaotu.base.model.po.Book;
import com.kaotu.base.model.po.Favorite;
import com.kaotu.base.model.po.UserTag;
import com.kaotu.base.model.vo.BookVO;
import com.kaotu.base.model.vo.CategoryVO;
import com.kaotu.user.mapper.BookMapper;
import com.kaotu.user.mapper.FavoriteMapper;
import com.kaotu.user.mapper.UserTagMapper;
import com.kaotu.user.service.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    @Autowired
    private UserTagMapper userTagMapper;

    @Override
    public BookVO getBookVOById(Integer bookId) {

        Book book= bookMapper.selectById(bookId);
        if (book==null){
            throw new BaseException("书籍不存在");
        }
        String categoryName = bookMapper.getCategoryNameById(book.getSubCategoryId());
        BookVO bookVO=new BookVO();
        BeanUtils.copyProperties(book,bookVO);
        bookVO.setCategoryName(categoryName);
        return bookVO;
    }

    @Override
    public List<BookVO> searchBooks(String searchParam) {
        if (searchParam==null|| searchParam.trim().isEmpty()) {
            throw new BaseException("搜索参数不能为空");
        }
        LambdaQueryWrapper<Book> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(Book::getTitle,searchParam)
                .or().like(Book::getPublisher,searchParam);
        List<BookVO> bookVOList = bookMapper.getBookVOList(queryWrapper);
        return bookVOList;
    }

    @Override
    public void collectBook(Integer bookId) {
        if (bookId == null || bookId <= 0) {
            throw new BaseException("无效的书籍ID");
        }
        if (favoriteMapper.selectOne(new LambdaQueryWrapper<Favorite>().eq(Favorite::getBookId,bookId)
                .eq(Favorite::getUserId,UserContext.getUserId())) != null){
            throw new BaseException("您已经收藏过此书籍");
        }

        Favorite favorite=new Favorite();
        favorite.setBookId(bookId);
        favorite.setUserId(UserContext.getUserId());
        int insert = favoriteMapper.insert(favorite);
        if (insert <= 0) {
            throw new BaseException("收藏书籍失败，请稍后再试");
        }
    }

    @Override
    public List<BookVO> getBookListByCategoryId(Integer categoryId) {
        if( categoryId == null || categoryId <= 0) {
            throw new BaseException("无效的分类ID");
        }
        LambdaQueryWrapper<Book> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Book::getSubCategoryId,categoryId);
        List<BookVO> bookVOList = bookMapper.getBookVOList(queryWrapper);
        return bookVOList;
    }

    @Override
    public List<CategoryVO> getAllCategories() {
        List<CategoryVO> categories = bookMapper.getAllCategories();

        return categories;
    }

    @Override
    @Transactional
    public void addTags(List<Integer> tags) {
        if (tags == null || tags.isEmpty()) {
            throw new BaseException("标签列表不能为空");
        }
        // 清空当前用户的标签
        LambdaQueryWrapper<UserTag> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(UserTag::getUserId,UserContext.getUserId());
        userTagMapper.delete(deleteWrapper);
        // 插入新的标签
        for(Integer tagId : tags){
            userTagMapper.insert(new UserTag(UserContext.getUserId(), tagId, WEIGHT.TAG_WEIGHT));
        }
    }

    @Override
    public List<BookVO> getHotList() {
        LambdaQueryWrapper<Book> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Book::getComments);
        queryWrapper.last("limit 10");
        List<BookVO> books= bookMapper.getBookVOList(queryWrapper);
        return books;
    }

    //TODO 应获取personal_recommend表的数据并返回
    @Override
    public List<BookVO> getPersonalize() {//伪推荐
        // 获取当前用户的标签
        List<UserTag> userTags = userTagMapper.selectList(new LambdaQueryWrapper<UserTag>().eq(UserTag::getUserId, UserContext.getUserId()));
        List<Integer> tagIds = new ArrayList<>();
        for(UserTag userTag : userTags) {
            tagIds.add(userTag.getTagId());
        }
        if (tagIds.isEmpty()) {
            // 如果没有标签，返回空列表
            return Collections.emptyList();
        }
        // 根据标签获取书籍
        LambdaQueryWrapper<Book> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Book::getSubCategoryId,tagIds);
        queryWrapper.orderByDesc(Book::getComments);
        queryWrapper.last("limit 10");
        List<BookVO> personalizedBooks = bookMapper.getBookVOList(queryWrapper);
        return personalizedBooks;
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
}
