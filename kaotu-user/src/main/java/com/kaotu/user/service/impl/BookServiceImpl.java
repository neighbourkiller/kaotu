package com.kaotu.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kaotu.base.context.UserContext;
import com.kaotu.base.exception.BaseException;
import com.kaotu.base.model.po.Book;
import com.kaotu.base.model.po.Favorite;
import com.kaotu.base.model.vo.BookVO;
import com.kaotu.base.model.vo.CategoryVO;
import com.kaotu.user.mapper.BookMapper;
import com.kaotu.user.mapper.FavoriteMapper;
import com.kaotu.user.service.BookService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
@Service
public class BookServiceImpl extends ServiceImpl<BookMapper, Book> implements BookService {

    @Autowired
    private BookMapper bookMapper;

    @Autowired
    private FavoriteMapper favoriteMapper;

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

}
