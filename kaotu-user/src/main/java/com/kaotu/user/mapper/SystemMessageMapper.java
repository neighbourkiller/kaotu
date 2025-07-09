package com.kaotu.user.mapper;

import com.kaotu.base.model.po.SystemMessage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;

/**
 * <p>
 * 系统消息表 Mapper 接口
 * </p>
 *
 * @author killer
 * @since 2025-07-08
 */
public interface SystemMessageMapper extends BaseMapper<SystemMessage> {

    @Insert("INSERT INTO system_message (receiver_id, title, content, type, is_read, create_time) " +
            "VALUES (#{userId}, #{title}, #{content}, #{type}, #{isRead}, #{time})")
    int insertSystemMessage(@Param("userId") String userId,
                            @Param("title") String title,
                            @Param("content") String content,
                            @Param("type") String type,
                            @Param("isRead") Boolean isRead,
                            @Param("time") LocalDateTime time);
}
