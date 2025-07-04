package com.kaotu.base.model.po;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 
 * </p>
 *
 * @author killer
 * @since 2025-07-03
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("book_category")
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 种类编号
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 种类名
     */
    private String categoryName;

    /**
     * 父种类编号
     */
    private Integer parentCategory;


}
