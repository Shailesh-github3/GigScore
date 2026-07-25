package com.org.gigscore.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ActivityResponse {
    private Long id;
    private Long userId;
    private String platform;
    private String action;
    private Double amount;
    private Double rating;
    private LocalDateTime timestamp;
}
