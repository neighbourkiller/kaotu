package com.kaotu.admin.model.dto;

import lombok.Data;

@Data
public class SearchPageDto {
    private Integer pageNo;
    private Integer pageSize;
    private String title;
    private String publisher;
}
