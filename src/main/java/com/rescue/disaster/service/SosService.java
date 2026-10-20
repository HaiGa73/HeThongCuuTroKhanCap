package com.rescue.disaster.service;

import com.rescue.disaster.dto.SosCreateRequestDto;
import com.rescue.disaster.entity.SosMedia;
import com.rescue.disaster.entity.SosRequest;
import com.rescue.disaster.repository.SosRequestRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@Service
public class SosService {

    private final SosRequestRepository sosRepository;
    private final CloudinaryService cloudinaryService;
    private final SimpMessagingTemplate messagingTemplate;

    // Thay thế cho @RequiredArgsConstructor
    @Autowired
    public SosService(SosRequestRepository sosRepository,
                      CloudinaryService cloudinaryService,
                      SimpMessagingTemplate messagingTemplate) {
        this.sosRepository = sosRepository;
        this.cloudinaryService = cloudinaryService;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public SosRequest createSos(SosCreateRequestDto dto) {
        SosRequest sos = SosRequest.builder()
                .senderName(dto.getSenderName() != null ? dto.getSenderName() : "Người dân gặp nạn")
                .phoneNumber(dto.getPhoneNumber())
                .addressDetail(dto.getAddressDetail())
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .numberOfPeople(dto.getNumberOfPeople() != null ? dto.getNumberOfPeople() : 1)
                .isUrgent(dto.getIsUrgent() != null ? dto.getIsUrgent() : true)
                .description(dto.getDescription())
                .status("PENDING")
                .priority(Boolean.TRUE.equals(dto.getIsUrgent()) ? "CRITICAL" : "HIGH")
                .build();

        if (dto.getFiles() != null && !dto.getFiles().isEmpty()) {
            for (MultipartFile file : dto.getFiles()) {
                if (!file.isEmpty()) {
                    try {
                        Map<String, Object> uploadResult = cloudinaryService.uploadFile(file, "sos_evidence");
                        String secureUrl = (String) uploadResult.get("secure_url");
                        String publicId = (String) uploadResult.get("public_id");

                        SosMedia media = SosMedia.builder()
                                .sosRequest(sos)
                                .mediaUrl(secureUrl)
                                .publicId(publicId)
                                .mediaType(file.getContentType() != null && file.getContentType().startsWith("audio") ? "AUDIO" : "IMAGE")
                                .build();

                        sos.getMediaList().add(media);
                    } catch (Exception e) {
                        log.error("Lỗi upload Cloudinary: {}", e.getMessage());
                    }
                }
            }
        }

        SosRequest savedSos = sosRepository.save(sos);
        messagingTemplate.convertAndSend("/topic/manager/sos-alerts", savedSos);
        return savedSos;
    }
}