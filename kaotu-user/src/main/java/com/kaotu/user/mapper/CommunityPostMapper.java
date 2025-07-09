package com.kaotu.user.mapper;

import com.kaotu.base.model.po.CommunityPost;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 社区帖子表 Mapper 接口
 * </p>
 *
 * @author killer
 * @since 2025-07-07
 */
@Mapper
public interface CommunityPostMapper extends BaseMapper<CommunityPost> {

    @Update("UPDATE kaotu.community_post SET like_count = like_count + #{change} WHERE id = #{id}")
    int updateLikeCount(@Param("id") Long targetId,@Param("change") int i);

    @Update("UPDATE kaotu.community_post SET collect_count = collect_count + #{change} WHERE id = #{id}")
    int updateCollectCount(@Param("id") Long postId,@Param("change") int i);

    @Update("UPDATE kaotu.community_post SET comment_count = comment_count + #{change} WHERE id = #{id}")
    int updateCommentCount(@Param("id") Long postId,@Param("change") int i);

    @Update("UPDATE kaotu.community_post SET view_count = view_count + #{change} WHERE id = #{id}")
    int updateViewCount(@Param("id") Long postId,@Param("change") int i);
}
