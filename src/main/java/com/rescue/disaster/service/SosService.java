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

import java.math.BigDecimal;
import java.util.Map;

@Slf4j
@Service
public class SosService {

    private final SosRequestRepository sosRepository;
    private final CloudinaryService cloudinaryService;
    private final SimpMessagingTemplate messagingTemplate;

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
        // Tọa độ dự phòng (trung tâm VN) nếu người dùng chưa kịp bật GPS trên trình duyệt
        BigDecimal lat = (dto.getLatitude() != null) ? dto.getLatitude() : new BigDecimal("16.047079");
        BigDecimal lng = (dto.getLongitude() != null) ? dto.getLongitude() : new BigDecimal("108.206230");

        SosRequest sos = SosRequest.builder()
                .senderName((dto.getSenderName() != null && !dto.getSenderName().isBlank()) ? dto.getSenderName() : "Người dân gặp nạn")
                .phoneNumber(dto.getPhoneNumber())
                .addressDetail(dto.getAddressDetail())
                .latitude(lat)
                .longitude(lng)
                .numberOfPeople(dto.getNumberOfPeople() != null ? dto.getNumberOfPeople() : 1)
                .isUrgent(dto.getIsUrgent() != null ? dto.getIsUrgent() : true)
                .description(dto.getDescription())
                .status("PENDING")
                .isDeleted(false)
                .priority(Boolean.TRUE.equals(dto.getIsUrgent()) ? "CRITICAL" : "HIGH")
                .build();

        // Xử lý upload ảnh/video hiện trường
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
                                .mediaType((file.getContentType() != null && file.getContentType().startsWith("audio")) ? "AUDIO" : "IMAGE")
                                .build();

                        sos.getMediaList().add(media);
                    } catch (Exception e) {
                        log.error("Lỗi upload Cloudinary: {}", e.getMessage());
                    }
                }
            }
        }

        SosRequest savedSos = sosRepository.save(sos);

        // Bắn WebSocket thông báo thời gian thực cho Manager
        messagingTemplate.convertAndSend("/topic/manager/sos-alerts", savedSos);

        return savedSos;
    }

    // Phương thức cập nhật trạng thái ca cứu nạn
    @Transactional
    public SosRequest updateSosStatus(Long sosId, String newStatus) {
        SosRequest sos = sosRepository.findById(sosId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy ca SOS #" + sosId));
        sos.setStatus(newStatus);
        SosRequest updated = sosRepository.save(sos);

        // Bắn WebSocket cập nhật giao diện thời gian thực
        messagingTemplate.convertAndSend("/topic/manager/sos-status-updated", updated);
        return updated;
    }
}