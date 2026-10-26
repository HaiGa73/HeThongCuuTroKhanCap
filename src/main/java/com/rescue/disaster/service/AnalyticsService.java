package com.rescue.disaster.service;

import com.rescue.disaster.dto.AnalyticsSummaryDto;
import com.rescue.disaster.repository.MissingPersonRepository;
import com.rescue.disaster.repository.SafeCheckinRepository;
import com.rescue.disaster.repository.SosRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsService {

    private final SosRequestRepository sosRequestRepository;
    private final SafeCheckinRepository safeCheckinRepository;
    private final MissingPersonRepository missingPersonRepository;

    @Autowired
    public AnalyticsService(SosRequestRepository sosRequestRepository,
                            SafeCheckinRepository safeCheckinRepository,
                            MissingPersonRepository missingPersonRepository) {
        this.sosRequestRepository = sosRequestRepository;
        this.safeCheckinRepository = safeCheckinRepository;
        this.missingPersonRepository = missingPersonRepository;
    }

    public AnalyticsSummaryDto getSummary() {
        return AnalyticsSummaryDto.builder()
                .totalSos(sosRequestRepository.countByIsDeletedFalse())
                .pendingSos(sosRequestRepository.countByStatusAndIsDeletedFalse("PENDING"))
                .inProgressSos(sosRequestRepository.countByStatusAndIsDeletedFalse("IN_PROGRESS"))
                .completedSos(sosRequestRepository.countByStatusAndIsDeletedFalse("COMPLETED"))
                .totalSafeCheckins(safeCheckinRepository.count())
                .searchingMissingPersons(missingPersonRepository.countByStatus("SEARCHING"))
                .foundMissingPersons(missingPersonRepository.countByStatus("FOUND"))
                .criticalPriority(sosRequestRepository.countByPriorityAndIsDeletedFalse("CRITICAL"))
                .highPriority(sosRequestRepository.countByPriorityAndIsDeletedFalse("HIGH"))
                .mediumPriority(sosRequestRepository.countByPriorityAndIsDeletedFalse("MEDIUM"))
                .lowPriority(sosRequestRepository.countByPriorityAndIsDeletedFalse("LOW"))
                .build();
    }
}