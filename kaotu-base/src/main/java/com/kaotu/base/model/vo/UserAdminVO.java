package com.kaotu.base.model.vo;

import lombok.Data;

@Data
public class UserAdminVO {
    private String userId;
    private String username;
    private String email;
    private String unblockTime;
    private String createDate;
    private String lastLogin;
}
