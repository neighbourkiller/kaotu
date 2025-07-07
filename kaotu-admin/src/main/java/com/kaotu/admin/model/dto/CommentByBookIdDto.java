package com.kaotu.admin.model.dto;

import lombok.Data;

@Data
public class CommentByBookIdDto {
    private Integer pageNo;
    private Integer pageSize;
    private Integer bookId;
}
