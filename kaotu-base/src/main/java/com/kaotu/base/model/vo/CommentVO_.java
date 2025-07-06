package com.kaotu.base.model.vo;

import lombok.Data;

import java.time.LocalDateTime;


@Data
public class CommentVO_ {
    private Integer id;
    private Integer bookId;
    private String userId;
    private String content;
    private LocalDateTime commentTime;
    private Integer star;
    private Integer ups;
    private Integer status;
    private String title;
    private String username;
    private Integer isUpvoted; // 是否已点赞，"1"或"0"
}
