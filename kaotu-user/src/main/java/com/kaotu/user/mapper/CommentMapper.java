package com.kaotu.user.mapper;

import com.kaotu.base.model.po.Comment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 评论 Mapper 接口
 * </p>
 *
 * @author killer
 * @since 2025-07-03
 */
public interface CommentMapper extends BaseMapper<Comment> {

    @Update("update comment  set ups=ups + #{change} where id = #{id}")
    int updateCommentUps(@Param("id") int commentId,@Param("change") int upsChange);
}
