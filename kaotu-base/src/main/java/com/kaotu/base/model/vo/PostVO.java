package com.kaotu.base.model.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostVO {
    private Long id;
    private String userId;
    private Integer bookId;
    private String title;
    private String content;
    private Integer viewCount;
    private Integer likeCount;
    private Integer collectCount;
    private Integer commentCount;
    private Boolean isTop;
    private Boolean status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    private String userName;
    private List<Integer> tagIds;

    private Boolean isLiked;
    private Boolean isCollected;
}
