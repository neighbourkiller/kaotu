package com.kaotu.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.kaotu.base.model.po.Book;
import com.kaotu.base.model.vo.BookVO;
import com.kaotu.base.model.vo.CategoryVO;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * <p>
 * 书籍信息 服务类
 * </p>
 *
 * @author killer
 * @since 2025-07-02
 */
public interface BookService extends IService<Book> {

    /**
     * 根据书籍ID获取书籍详细信息
     *
     * @param bookId 书籍ID
     * @return BookVO 包含书籍详细信息的视图对象
     */
    BookVO getBookVOById(@RequestParam Integer bookId);

    /**
     * 根据关键词搜索书籍
     *
     * @param searchParam 搜索关键词
     * @return List<BookVO> 包含搜索结果的书籍视图对象列表
     */
    List<BookVO> searchBooks(@RequestParam String searchParam);

    /**
     * 收藏指定的书籍
     *
     * @param bookId 书籍ID
     */
    void collectBook(@RequestParam Integer bookId);

    /**
     * 根据分类ID获取书籍列表
     *
     * @param categoryId 分类ID
     * @return List<BookVO> 包含指定分类下的书籍视图对象列表
     */
    List<BookVO> getBookListByCategoryId(Integer categoryId);

    /**
     * 获取所有书籍分类
     *
     * @return List<CategoryVO> 包含所有书籍分类的视图对象列表
     */
    List<CategoryVO> getAllCategories();

    /**
     * 用户选择标签
     * @param tags
     */
    void addTags(List<Integer> tags);

    /**
     * 获取热门书籍列表
     *
     * @return List<BookVO> 包含热门书籍的视图对象列表
     */
    List<BookVO> getHotList();

    /**
     * 获取个性化推荐书籍列表
     *
     * @return List<BookVO> 包含个性化推荐书籍的视图对象列表
     */
    List<BookVO> getPersonalize();
}
