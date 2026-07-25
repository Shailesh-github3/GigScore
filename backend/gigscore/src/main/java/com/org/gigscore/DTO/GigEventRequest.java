package com.org.gigscore.DTO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class GigEventRequest {
    private Long userId;

    @NotBlank(message = "Platform is required")
    private String platform;

    @Positive(message = "Amount must be positive")
    private Double amount;

    @Min(value = 0, message = "Rating must be at least 0")
    @Max(value = 5, message = "Rating must be at most 5")
    private Double rating;
}