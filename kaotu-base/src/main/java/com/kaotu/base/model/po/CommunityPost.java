package com.kaotu.base.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 社区帖子表
 * </p>
 *
 * @author killer
 * @since 2025-07-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class CommunityPost implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String userId;

    private Integer bookId;

    /**
     * 标题
     */
    private String title;

    private String content;

    /**
     * 浏览数
     */
    private Integer viewCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 收藏数
     */
    private Integer collectCount;

    private Integer commentCount;

    /**
     * 是否置顶
     */
    @TableField("is_top")
    private Boolean isTop;

    private Boolean status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;


}
