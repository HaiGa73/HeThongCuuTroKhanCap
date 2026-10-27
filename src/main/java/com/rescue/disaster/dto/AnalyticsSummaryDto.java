package com.rescue.disaster.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsSummaryDto {
    private long totalSos;
    private long pendingSos;
    private long inProgressSos;
    private long completedSos;

    private long totalSafeCheckins;
    private long searchingMissingPersons;
    private long foundMissingPersons;

    private long criticalPriority;
    private long highPriority;
    private long mediumPriority;
    private long lowPriority;

    public long getSafeCheckinsCount() {
        return totalSafeCheckins;
    }

    public long getMissingPersonsCount() {
        return searchingMissingPersons;
    }

    public long getFoundPersonsCount() {
        return foundMissingPersons;
    }
}