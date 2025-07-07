package com.kaotu.admin.mapper;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kaotu.base.model.po.Admin;
import com.kaotu.base.model.po.Book;
import com.kaotu.base.model.po.Comment;
import com.kaotu.base.model.vo.BookVO;
import com.kaotu.base.model.vo.CommentVO;
import org.apache.ibatis.annotations.*;

@Mapper
public interface AdminMapper extends BaseMapper<Admin> {

    @Select("SELECT * FROM admin WHERE admin_id = #{adminId}")
    public Admin queryAdminById(String adminId);

    Page<BookVO> selectBookPageWithCategory(Page<BookVO> page, @Param("ew") QueryWrapper<BookVO> queryWrapper);

    Page<CommentVO> selectCommentPage(Page<CommentVO> page, @Param("ew") QueryWrapper<Book> queryWrapper);

    @Select("select * from kaotu.comment where id = #{commentId}")
    Comment selectCommentById(@Param("commentId") Long commentId);

    @Update("UPDATE kaotu.comment SET status = #{status} WHERE id = #{id}")
    int updateCommentById(Comment comment);

    @Delete("DELETE FROM kaotu.comment WHERE id = #{commentId}")
    int deleteCommentById(Long commentId);


    int updateCommentStatusByBookId(LambdaUpdateWrapper<Comment> lambdaUpdateWrapper);
}
