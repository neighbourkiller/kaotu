package com.kaotu.base.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 热门帖子
 * </p>
 *
 * @author killer
 * @since 2025-07-09
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class HotPost implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 字符串形式的数组，以“,”隔开
     */
    private String postIds;

    /**
     * 创造日期
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDate calculationDate;


}
