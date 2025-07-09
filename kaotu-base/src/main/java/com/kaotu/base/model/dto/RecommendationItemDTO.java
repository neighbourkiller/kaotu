package com.kaotu.base.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class RecommendationItemDTO {

    private Integer id;
    private String title;
    private String publisher;

    @JsonProperty("comment_count")
    private Integer commentCount;

    private List<String> categories;

    @JsonProperty("book_url")
    private String bookUrl;

    @JsonProperty("img_url")
    private String imgUrl;

    private List<String> tags;

    @JsonProperty("recommend_reason")
    private String recommendReason;
}
