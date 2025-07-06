package com.kaotu.base.model.vo;

import lombok.Data;

@Data
public class SubCategoryVO {
    private Integer id;
    private String categoryName;
    private Integer parentCategory;
    private Integer isSelected; // 0: 未选中, 1: 已选中
}
