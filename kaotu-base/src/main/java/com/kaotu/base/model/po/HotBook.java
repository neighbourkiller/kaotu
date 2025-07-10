package com.kaotu.base.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 热点推荐
 * </p>
 *
 * @author killer
 * @since 2025-07-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class HotBook implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String bookIds;

    private LocalDate calculationDate;


}
