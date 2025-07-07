package com.kaotu.base.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostTagVO {
    private Integer id;

    /**
     * 标签名称
     */
    private String name;

    /**
     * 标签分类
     */
    private Integer categoryId;
    private String categoryName;

    private LocalDateTime createTime;
}
