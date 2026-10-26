package com.rescue.disaster.service;

import com.rescue.disaster.dto.SosCreateRequestDto;
import com.rescue.disaster.entity.SosMedia;
import com.rescue.disaster.entity.SosRequest;
import com.rescue.disaster.entity.User;
import com.rescue.disaster.repository.SosRequestRepository;
import com.rescue.disaster.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class SosService {

    private final SosRequestRepository sosRequestRepository;
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public SosService(SosRequestRepository sosRequestRepository,
                      UserRepository userRepository,
                      CloudinaryService cloudinaryService,
                      SimpMessagingTemplate messagingTemplate) {
        this.sosRequestRepository = sosRequestRepository;
        this.userRepository = userRepository;
        this.cloudinaryService = cloudinaryService;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public SosRequest createSos(SosCreateRequestDto dto) {
        User currentUser = null;
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                currentUser = userRepository.findByUsername(auth.getName()).orElse(null);
            }
        } catch (Exception ignored) {}

        // Chuyển dấu phẩy thành dấu chấm tránh NumberFormatException
        BigDecimal lat = BigDecimal.ZERO;
        BigDecimal lng = BigDecimal.ZERO;
        try {
            if (dto.getLatitude() != null && !dto.getLatitude().isBlank()) {
                lat = new BigDecimal(dto.getLatitude().trim().replace(',', '.'));
            }
            if (dto.getLongitude() != null && !dto.getLongitude().isBlank()) {
                lng = new BigDecimal(dto.getLongitude().trim().replace(',', '.'));
            }
        } catch (Exception e) {
            log.warn("Lỗi parse tọa độ, dùng 0.0: {}", e.getMessage());
        }

        SosRequest sos = SosRequest.builder()
                .user(currentUser)
                .senderName(dto.getSenderName() != null ? dto.getSenderName() : "Người dân")
                .phoneNumber(dto.getPhoneNumber() != null ? dto.getPhoneNumber() : "Chưa có")
                .latitude(lat)
                .longitude(lng)
                .addressDetail(dto.getAddressDetail())
                .numberOfPeople(dto.getNumberOfPeople() != null ? dto.getNumberOfPeople() : 1)
                .priority(dto.getPriority() != null ? dto.getPriority() : "HIGH")
                .description(dto.getDescription())
                .status("PENDING")
                .mediaList(new ArrayList<>())
                .build();

        // Tải ảnh lên Cloudinary
        if (dto.getFiles() != null && !dto.getFiles().isEmpty()) {
            for (MultipartFile file : dto.getFiles()) {
                if (file != null && !file.isEmpty()) {
                    try {
                        Map<String, Object> uploadResult = cloudinaryService.uploadFile(file, "sos_media");
                        String url = (String) uploadResult.get("secure_url");
                        String publicId = (String) uploadResult.get("public_id");

                        SosMedia media = SosMedia.builder()
                                .sosRequest(sos)
                                .mediaUrl(url)
                                .mediaType("IMAGE")
                                .publicId(publicId)
                                .build();
                        sos.getMediaList().add(media);
                    } catch (Exception e) {
                        log.error("Lỗi upload Cloudinary: {}", e.getMessage());
                    }
                }
            }
        }

        SosRequest savedSos = sosRequestRepository.save(sos);

        // Bắn WebSocket thông báo ca mới
        try {
            messagingTemplate.convertAndSend("/topic/manager/sos-alerts", savedSos);
        } catch (Exception e) {
            log.error("Lỗi gửi WebSocket: {}", e.getMessage());
        }

        return savedSos;
    }

    @Transactional
    public SosRequest createSosRequest(SosCreateRequestDto dto, List<MultipartFile> files) {
        if (files != null && !files.isEmpty()) {
            dto.setFiles(files);
        }
        return createSos(dto);
    }

    @Transactional
    public SosRequest updateSosStatus(Long id, String newStatus) {
        SosRequest sos = sosRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ca cứu nạn có ID: " + id));

        sos.setStatus(newStatus);
        SosRequest updated = sosRequestRepository.save(sos);

        try {
            messagingTemplate.convertAndSend("/topic/manager/sos-status-updated", updated);
        } catch (Exception ignored) {}

        return updated;
    }

    @Transactional
    public SosRequest cancelSosByUser(Long sosId, String username, String cancelReason) {
        SosRequest sos = sosRequestRepository.findById(sosId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ca cứu nạn"));

        if (sos.getUser() == null || !sos.getUser().getUsername().equals(username)) {
            throw new SecurityException("Bạn không có quyền can thiệp vào ca cứu nạn này!");
        }

        sos.setStatus("CANCELLED");
        sos.setDescription((sos.getDescription() != null ? sos.getDescription() : "") + " | [HỦY BỞI NGƯỜI DÙNG: " + cancelReason + "]");
        SosRequest updated = sosRequestRepository.save(sos);

        try {
            messagingTemplate.convertAndSend("/topic/manager/sos-status-updated", updated);
        } catch (Exception ignored) {}

        return updated;
    }
}