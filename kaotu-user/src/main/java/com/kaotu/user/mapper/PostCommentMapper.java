package com.kaotu.user.mapper;

import com.kaotu.base.model.po.PostComment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 社区帖子评论表 Mapper 接口
 * </p>
 *
 * @author killer
 * @since 2025-07-07
 */
public interface PostCommentMapper extends BaseMapper<PostComment> {

    int updateLikeCount(@Param("id") Long targetId,@Param("change") int i);

    @Update("UPDATE kaotu.post_comment SET status = #{status} WHERE post_id = #{postId}")
    int updateCommentStatus(@Param("postId") Long postId, @Param("status") Boolean status);

    @Update("UPDATE kaotu.post_comment SET status = #{status} WHERE parent_id = #{parentId}")
    int updateSubCommentStatus(@Param("parentId") Long parentId,@Param("status") Boolean status);
}
