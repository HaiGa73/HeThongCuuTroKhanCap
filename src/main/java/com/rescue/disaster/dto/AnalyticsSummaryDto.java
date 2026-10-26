package com.rescue.disaster.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AnalyticsSummaryDto {
    // KPI Cards
    private long totalSos;
    private long pendingSos;
    private long inProgressSos;
    private long completedSos;
    private long totalSafeCheckins;
    private long searchingMissingPersons;
    private long foundMissingPersons;

    // Cơ cấu mức độ ưu tiên
    private long criticalPriority;
    private long highPriority;
    private long mediumPriority;
    private long lowPriority;
}