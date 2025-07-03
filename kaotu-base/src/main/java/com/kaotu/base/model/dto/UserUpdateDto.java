package com.kaotu.base.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserUpdateDto {
    private String userId;
    private String username;
    private String password;
    private String email;
    private LocalDateTime unblockTime;
}
