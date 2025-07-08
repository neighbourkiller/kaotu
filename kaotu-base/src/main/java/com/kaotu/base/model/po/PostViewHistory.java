package com.kaotu.base.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 帖子浏览记录表
 * </p>
 *
 * @author killer
 * @since 2025-07-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PostViewHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 记录主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 浏览用户的ID
     */
    private String userId;

    /**
     * 被浏览的帖子ID
     */
    private Long postId;

    /**
     * 浏览时间
     */
    private LocalDateTime viewTime;

    /**
     * 浏览时长s
     */
    private Integer timeLength;


}
