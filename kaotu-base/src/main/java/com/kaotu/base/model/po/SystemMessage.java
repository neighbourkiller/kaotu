package com.kaotu.base.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 系统消息表
 * </p>
 *
 * @author killer
 * @since 2025-07-08
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class SystemMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 接收消息的用户ID
     */
    private String receiverId;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 消息类型 (例如: SYSTEM_ANNOUNCEMENT, POST_LIKE, NEW_COMMENT, NEW_FOLLOWER)
     */
    private String type;

    /**
     * 是否已读 (0: 未读, 1: 已读)
     */
//    @TableField("is_read")
    private Boolean isRead;

    /**
     * 消息创建时间
     */
    private LocalDateTime createTime;


}
