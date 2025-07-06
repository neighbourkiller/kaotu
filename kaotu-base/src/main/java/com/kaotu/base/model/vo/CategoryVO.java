package com.kaotu.base.model.vo;

import lombok.Data;

import java.util.List;

@Data
public class CategoryVO {
    private Integer id;
    private String name;
    private List<SubCategoryVO> subCategory;
}
