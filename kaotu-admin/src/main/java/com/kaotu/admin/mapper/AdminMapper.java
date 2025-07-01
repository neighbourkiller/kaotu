package com.kaotu.admin.mapper;

import com.kaotu.base.model.po.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdminMapper {

    @Select("SELECT * FROM admin WHERE admin_id = #{adminId}")
    public Admin queryAdminById(String adminId);
}
