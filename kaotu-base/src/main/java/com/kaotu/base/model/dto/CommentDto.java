package com.kaotu.base.model.dto;

import lombok.Data;

@Data
public class CommentDto {
    private Integer bookId;
    private String userId;
    private String content;
    private Integer Star;
}
