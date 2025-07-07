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
}
