package com.kaotu.user.mapper;

import com.kaotu.base.model.po.PostTag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 帖子-标签关联表 Mapper 接口
 * </p>
 *
 * @author killer
 * @since 2025-07-07
 */
public interface PostTagMapper extends BaseMapper<PostTag> {

    @Select("SELECT tag_id FROM kaotu.post_tag WHERE post_id = #{postId}")
    List<Integer> getTagIdsByPostId(Long postId);
}
