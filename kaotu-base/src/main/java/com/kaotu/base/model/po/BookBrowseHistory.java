package com.kaotu.base.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.time.LocalDateTime;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 书籍浏览历史
 * </p>
 *
 * @author killer
 * @since 2025-07-10
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class BookBrowseHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 书籍id
     */
    private Integer bookId;

    /**
     * 用户id
     */
    private String userId;

    private LocalDateTime browseTime;

    /**
     * 浏览时长
     */
    private Integer duration;


}
