package com.org.gigscore.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScoreResponse {
    private Long userId;
    private Double score;
}
