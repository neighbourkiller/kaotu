package com.kaotu.base.model.vo;

import lombok.Data;

@Data
public class BookVO {
    private Integer id;
    private String title;
    private String publisher;
    private Integer comments;
    private String bookUrl;
    private String imgUrl;
    private Integer subCategoryId;
    private String categoryName;
    private Integer isCollected;
}
