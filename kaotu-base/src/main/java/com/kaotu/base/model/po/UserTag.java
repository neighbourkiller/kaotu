package com.kaotu.base.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 用户标签关联表
 * </p>
 *
 * @author killer
 * @since 2025-07-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class UserTag implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 标签编号
     */
    private Integer tagId;

    private Integer weight;

}
