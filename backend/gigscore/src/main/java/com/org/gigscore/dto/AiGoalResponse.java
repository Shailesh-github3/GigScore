package com.org.gigscore.dto;

import lombok.Data;

@Data
public class AiGoalResponse {
    private String message;
    private Integer targetScore;
    private String estimatedTimeline;
}
