package com.kaotu.admin.model.dto;

import lombok.Data;

@Data
public class UserPageDto {
    private Integer pageNo;
    private Integer pageSize;
    private String userId;
}
