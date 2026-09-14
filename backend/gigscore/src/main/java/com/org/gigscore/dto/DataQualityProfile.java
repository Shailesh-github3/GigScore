package com.org.gigscore.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DataQualityProfile {
    private Integer numberOfGigs;
    private Integer activeDays;
    private ConfidenceLevel confidence;
    private List<String> flags;
    private String note;
}