package com.kaotu.base.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class RecommendationResponseDTO {

    @JsonProperty("user_id")
    private Integer userId;

    private List<RecommendationItemDTO> recommendations;
}