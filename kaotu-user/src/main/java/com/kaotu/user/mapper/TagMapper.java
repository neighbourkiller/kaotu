package com.kaotu.user.mapper;

import com.kaotu.base.model.po.Tag;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kaotu.base.model.vo.PostTagVO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 帖子标签表 Mapper 接口
 * </p>
 *
 * @author killer
 * @since 2025-07-07
 */
public interface TagMapper extends BaseMapper<Tag> {

    List<PostTagVO> getAllTags();
}
