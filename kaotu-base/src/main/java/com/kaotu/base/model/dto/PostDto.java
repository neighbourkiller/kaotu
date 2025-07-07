package com.kaotu.base.model.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostDto {
    private String title;
    private String content;
    private LocalDateTime createTime;
    private Integer bookId;
    private List<Integer> tags;
}
