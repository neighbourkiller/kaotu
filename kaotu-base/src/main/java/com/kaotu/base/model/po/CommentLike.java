package com.kaotu.base.model.po;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
* 
* @TableName comment_like
*/
@Data
@EqualsAndHashCode(callSuper = false)
public class CommentLike implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
    * 
    */

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;
    /**
    * 
    */
    private String userId;
    /**
    * 
    */
    private Long commentId;
    /**
    * 
    */
    private LocalDateTime createTime;

}
