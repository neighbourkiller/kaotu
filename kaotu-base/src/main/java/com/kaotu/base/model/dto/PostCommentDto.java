package com.kaotu.base.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostCommentDto {
    private Long postId;
    private Long parentId;
    private  String content;
    private LocalDateTime createTime;
}
