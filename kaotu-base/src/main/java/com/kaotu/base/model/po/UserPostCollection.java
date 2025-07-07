package com.kaotu.base.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 用户帖子收藏表
 * </p>
 *
 * @author killer
 * @since 2025-07-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UserPostCollection implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 收藏记录ID，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private String userId;

    /**
     * 帖子ID
     */
    private Long postId;

    /**
     * 收藏时间
     */
    private LocalDateTime createTime;


}
