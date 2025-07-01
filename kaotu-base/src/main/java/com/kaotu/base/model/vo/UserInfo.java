package com.kaotu.base.model.vo;

import lombok.Data;

@Data
public class UserInfo {
    private String userId;
    private String userName;
    private String email;
    private String unblockTime;
    private String lastLogin;
}
