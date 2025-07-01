package com.kaotu.admin.service;

import com.kaotu.base.model.dto.PageParams;
import com.kaotu.base.model.po.Admin;
import com.kaotu.base.model.po.User;
import com.kaotu.base.result.PageResult;

public interface AdminService {
    public String login(Admin admin);
    public PageResult<User> listUsers(PageParams pageParams);
}
