package com.rescue.disaster.service;

import com.rescue.disaster.dto.EmergencyAlertDto;
import com.rescue.disaster.entity.EmergencyAlert;
import com.rescue.disaster.repository.EmergencyAlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AlertService {

    private final EmergencyAlertRepository alertRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public AlertService(EmergencyAlertRepository alertRepository, SimpMessagingTemplate messagingTemplate) {
        this.alertRepository = alertRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public EmergencyAlert broadcastAlert(EmergencyAlertDto dto) {
        EmergencyAlert alert = EmergencyAlert.builder()
                .title(dto.getTitle())
                .content(dto.getContent())
                .severity(dto.getSeverity() != null ? dto.getSeverity() : "DANGER")
                .regionId(dto.getRegionId())
                .build();

        EmergencyAlert savedAlert = alertRepository.save(alert);

        // BẮN TIN KHẨN CẤP QUA WEBSOCKET ĐẾN TOÀN BỘ NGƯỜI DÙNG ĐANG ONLINE
        messagingTemplate.convertAndSend("/topic/emergency-broadcast", savedAlert);

        return savedAlert;
    }

    public List<EmergencyAlert> getAllAlerts() {
        return alertRepository.findAllByOrderByCreatedAtDesc();
    }
}