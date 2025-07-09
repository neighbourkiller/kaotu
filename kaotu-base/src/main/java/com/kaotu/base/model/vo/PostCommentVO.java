package com.kaotu.base.model.vo;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostCommentVO {
    private Long id;
    private Long postId;
    private Long parentId;
    private String userId;
    private String username; // 评论者用户名
    private String content;
    private LocalDateTime createTime;
    private Integer likeCount;
    private Boolean isLiked; // 是否已点赞
    private List<PostCommentVO> children; // 子评论列表
}
