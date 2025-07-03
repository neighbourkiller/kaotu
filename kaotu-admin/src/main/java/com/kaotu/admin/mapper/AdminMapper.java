package com.kaotu.admin.mapper;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kaotu.base.model.po.Admin;
import com.kaotu.base.model.vo.BookVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdminMapper extends BaseMapper<Admin> {

    @Select("SELECT * FROM admin WHERE admin_id = #{adminId}")
    public Admin queryAdminById(String adminId);

    Page<BookVO> selectBookPageWithCategory(Page<BookVO> page, @Param("ew") QueryWrapper<BookVO> queryWrapper);
}
