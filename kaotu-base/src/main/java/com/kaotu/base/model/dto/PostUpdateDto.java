package com.kaotu.base.model.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostUpdateDto {
    private Long postId;
    private String title;
    private String content;
    private LocalDateTime updateTime;
    private Integer bookId;
    private List<Integer> tags;
}
