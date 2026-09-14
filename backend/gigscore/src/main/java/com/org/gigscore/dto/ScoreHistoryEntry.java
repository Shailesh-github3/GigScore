package com.org.gigscore.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ScoreHistoryEntry {
    private Double score;
    private LocalDateTime recordedAt;
}
