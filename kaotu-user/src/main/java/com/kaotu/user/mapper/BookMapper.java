package com.kaotu.user.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kaotu.base.exception.BaseException;
import com.kaotu.base.model.po.Book;
import com.kaotu.base.model.po.Category;
import com.kaotu.base.model.vo.BookVO;
import com.kaotu.base.model.vo.CategoryVO;
import com.kaotu.base.model.vo.SubCategoryVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * <p>
 * 书籍信息 Mapper 接口
 * </p>
 *
 * @author killer
 * @since 2025-07-02
 */
@Mapper
public interface BookMapper extends BaseMapper<Book> {

    @Select("select category_name from kaotu.book_category where id = #{categoryId}")
    String getCategoryNameById(@RequestParam Integer categoryId);

    /**
     * 搜索书籍，bug：sql语句含有in id时，出现id二义性
     * @param queryWrapper
     * @return
     */
    List<BookVO> getBookVOList(@Param("ew") LambdaQueryWrapper<Book> queryWrapper);

    List<BookVO> getBookVOList2(@Param("ew") QueryWrapper<Book> queryWrapper);

    List<CategoryVO> getAllCategories(@Param("userId") String userId);

    List<SubCategoryVO> selectSubCategoriesByParentId(@Param("parentId") Integer parentId,@Param("userId") String userId);

}
