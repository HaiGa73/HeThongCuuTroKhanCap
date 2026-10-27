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
            log.warn("Tọa độ rỗng hoặc sai định dạng: {}", e.getMessage());
        }

        // Tự động phân cấp ưu tiên dựa trên tình huống người dân chọn
        String calculatedPriority = "MEDIUM";
        String type = dto.getEmergencyType() != null ? dto.getEmergencyType() : "OTHER";

        if ("INJURY_EMERGENCY".equals(type) || "ROOF_SUBMERGED".equals(type) || "LANDSLIDE_COLLAPSE".equals(type)) {
            calculatedPriority = "CRITICAL"; // Ưu tiên số 1: Nguy hiểm tính mạng
        } else if ("FAST_RISING_VULNERABLE".equals(type) || "FAST_FLOW_ISOLATED".equals(type)) {
            calculatedPriority = "HIGH";     // Ưu tiên số 2: Nước dâng / cô lập
        } else if ("OUT_OF_SUPPLIES".equals(type)) {
            calculatedPriority = "MEDIUM";   // Cần tiếp tế
        } else {
            calculatedPriority = "LOW";      // Hỗ trợ di dời thường
        }

        SosRequest sos = SosRequest.builder()
                .user(currentUser)
                .senderName(dto.getSenderName() != null ? dto.getSenderName() : "Người dân")
                .phoneNumber(dto.getPhoneNumber() != null ? dto.getPhoneNumber() : "Chưa có")
                .latitude(lat)
                .longitude(lng)
                .addressDetail(dto.getAddressDetail())
                .numberOfPeople(dto.getNumberOfPeople() != null ? dto.getNumberOfPeople() : 1)
                .priority(calculatedPriority)
                .description("[" + mapTypeToText(type) + "] - " + (dto.getDescription() != null ? dto.getDescription() : ""))
                .status("PENDING")
                .mediaList(new ArrayList<>())
                .build();

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

        try {
            messagingTemplate.convertAndSend("/topic/manager/sos-alerts", savedSos);
        } catch (Exception ignored) {}

        return savedSos;
    }

    private String mapTypeToText(String type) {
        return switch (type) {
            case "INJURY_EMERGENCY" -> "Có người bị thương/Đau ốm nguy kịch";
            case "ROOF_SUBMERGED" -> "Nước ngập chạm mái/Đang trên nóc nhà";
            case "LANDSLIDE_COLLAPSE" -> "Sạt lở đất/Sập nhà một phần";
            case "FAST_RISING_VULNERABLE" -> "Nước dâng nhanh (Có người già, trẻ nhỏ)";
            case "FAST_FLOW_ISOLATED" -> "Bị cô lập giữa dòng nước chảy xiết";
            case "OUT_OF_SUPPLIES" -> "Hết lương thực, nước uống";
            case "NEED_EVACUATION" -> "Cần hỗ trợ di dời phòng ngừa";
            default -> "Tình huống khác";
        };
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
    public SosRequest updateSosHandling(Long id, String newStatus, String note) {
        SosRequest sos = sosRequestRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy ca cứu nạn có ID: " + id));

        sos.setStatus(newStatus);
        if (note != null) {
            sos.setManagerNote(note.trim());
        }
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