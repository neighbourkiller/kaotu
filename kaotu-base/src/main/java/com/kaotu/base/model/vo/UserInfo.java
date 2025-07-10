package com.kaotu.base.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserInfo {
    private String userId;
    private String username;
    private String email;
    private LocalDateTime unblockTime;
    private LocalDateTime lastLogin;
}
