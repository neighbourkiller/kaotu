package com.kaotu.base.model.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 * 帖子-标签关联表
 * </p>
 *
 * @author killer
 * @since 2025-07-07
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class PostTag implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long postId;

    private Integer tagId;


}
