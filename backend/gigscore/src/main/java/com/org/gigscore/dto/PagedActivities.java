package com.org.gigscore.dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PagedActivities {
    private List<ActivityResponse> content;
    private int page;
    private int size;
    private long totalElements;
}