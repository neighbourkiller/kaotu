package com.kaotu.base.model.po;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 收藏
 * </p>
 *
 * @author killer
 * @since 2025-07-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Favorite implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String userId;

    private Integer bookId;

    /**
     * 收藏时间
     */
    @TableField(fill= FieldFill.INSERT)
    private LocalDateTime favoriteTime;


}
