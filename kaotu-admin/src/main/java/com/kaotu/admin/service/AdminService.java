package com.kaotu.admin.service;

import com.kaotu.base.model.dto.PageParams;
import com.kaotu.base.model.dto.UserUpdateDto;
import com.kaotu.base.model.po.Admin;
import com.kaotu.base.model.po.User;
import com.kaotu.base.model.vo.BookVO;
import com.kaotu.base.result.PageResult;

public interface AdminService {
    String login(Admin admin);
    PageResult<User> listUsers(PageParams pageParams);
    void updateUser(UserUpdateDto userUpdateDto) throws Exception;
    void deleteUser(String userId);
    PageResult<BookVO> listBooks(PageParams pageParams);
}
