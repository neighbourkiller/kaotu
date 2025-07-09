package com.kaotu.base.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 浏览历史视图对象
 * 用于展示用户浏览过的帖子历史记录
 */
@Data
public class ViewHistory {
    private Long id;
    private Long postId;
    private String title;
    private String content;
    private LocalDateTime viewTime;
    private String userId;// 发帖人id
    private String username;// 发帖人用户名
}
