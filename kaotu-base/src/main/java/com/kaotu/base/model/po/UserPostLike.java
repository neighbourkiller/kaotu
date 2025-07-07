package com.kaotu.base.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 用户帖子点赞表
 * </p>
 *
 * @author killer
 * @since 2025-07-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UserPostLike implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 点赞记录ID，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 点赞用户ID
     */
    private String userId;

    /**
     * 点赞对象ID (如帖子ID、评论ID)
     */
    private Long targetId;

    /**
     * 点赞对象类型 (0: 帖子, 1: 评论)
     */
    private Boolean targetType;

    /**
     * 点赞时间
     */
    private LocalDateTime createTime;


}
