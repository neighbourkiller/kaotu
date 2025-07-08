package com.kaotu.user.mapper;

import com.kaotu.base.model.po.Comment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kaotu.base.model.vo.CommentVO_;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 * 评论 Mapper 接口
 * </p>
 *
 * @author killer
 * @since 2025-07-03
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    @Update("update comment  set ups=ups + #{change} where id = #{id}")
    int updateCommentUps(@Param("id") Long commentId,@Param("change") int upsChange);

    @Select("    SELECT c.id AS comment_id, c.content, c.comment_time,c.user_id,IFNULL(u.username, '已注销用户') AS username,b.title AS book_title," +
            "CASE WHEN cl.user_id IS NOT NULL THEN 1 ELSE 0 END AS is_upvoted FROM comment c LEFT JOIN user u ON c.user_id = u.user_id " +
            "LEFT JOIN book b ON c.book_id = b.id LEFT JOIN comment_like cl ON c.id = cl.comment_id AND cl.user_id = #{userId} " +
            "WHERE c.book_id = #{bookId} AND c.status = 1 ORDER BY c.comment_time DESC;")
    List<CommentVO_> getCommentVOByBookId(@Param("bookId") Integer bookId, @Param("page") int page, @Param("size") int size);
}
