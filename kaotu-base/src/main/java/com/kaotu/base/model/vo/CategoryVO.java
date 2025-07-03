package com.kaotu.base.model.vo;

import com.kaotu.base.model.po.Category;
import lombok.Data;

import java.util.List;

@Data
public class CategoryVO {
    private Integer id;
    private String name;
    private List<Category> subCategory;
}
