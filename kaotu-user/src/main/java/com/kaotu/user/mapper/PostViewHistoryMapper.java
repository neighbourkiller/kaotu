package com.kaotu.user.mapper;

import com.kaotu.base.model.po.PostViewHistory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;

/**
 * <p>
 * 帖子浏览记录表 Mapper 接口
 * </p>
 *
 * @author killer
 * @since 2025-07-08
 */
public interface PostViewHistoryMapper extends BaseMapper<PostViewHistory> {

    @Update("UPDATE kaotu.post_view_history SET view_time=#{now},time_length=time_length +#{time} WHERE id=#{id}")
    int updateViewTime(@Param("id") Long id,@Param("now") LocalDateTime now,@Param("time") Integer time);
}
