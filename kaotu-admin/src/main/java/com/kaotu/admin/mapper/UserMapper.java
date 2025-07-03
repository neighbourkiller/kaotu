package com.kaotu.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.kaotu.base.model.po.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * 用户信息表 Mapper 接口
 * </p>
 *
 * @author killer
 * @since 2025-06-30
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

//    @Select("SELECT * FROM user WHERE user_id = #{userId}")
//    User getByUserId(String userId);

    @Update("UPDATE user SET unblock_time = NULL WHERE user_id = #{userId}")
    int updateUnblockTimeNULL(String userId);
}
