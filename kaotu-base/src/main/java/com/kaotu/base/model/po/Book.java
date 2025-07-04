package com.kaotu.base.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 书籍信息
 * </p>
 *
 * @author killer
 * @since 2025-07-02
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class Book implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 书籍编号
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 书名
     */
    private String title;

    /**
     * 出版社
     */
    private String publisher;

    /**
     * 评论数
     */
    private Integer comments;

    /**
     * 购买链接
     */
    private String bookUrl;

    /**
     * 封面地址
     */
    private String imgUrl;

    /**
     * 种类编号
     */
    private Integer subCategoryId;


}
