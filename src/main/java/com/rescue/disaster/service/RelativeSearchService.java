package com.rescue.disaster.service;

import com.rescue.disaster.dto.MissingPersonDto;
import com.rescue.disaster.dto.SafeCheckinDto;
import com.rescue.disaster.entity.MissingPerson;
import com.rescue.disaster.entity.SafeCheckin;
import com.rescue.disaster.entity.Shelter;
import com.rescue.disaster.repository.MissingPersonRepository;
import com.rescue.disaster.repository.SafeCheckinRepository;
import com.rescue.disaster.repository.ShelterRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RelativeSearchService {

    private final SafeCheckinRepository safeCheckinRepository;
    private final MissingPersonRepository missingPersonRepository;
    private final ShelterRepository shelterRepository;
    private final CloudinaryService cloudinaryService;

    @Autowired
    public RelativeSearchService(SafeCheckinRepository safeCheckinRepository,
                                 MissingPersonRepository missingPersonRepository,
                                 ShelterRepository shelterRepository,
                                 CloudinaryService cloudinaryService) {
        this.safeCheckinRepository = safeCheckinRepository;
        this.missingPersonRepository = missingPersonRepository;
        this.shelterRepository = shelterRepository;
        this.cloudinaryService = cloudinaryService;
    }

    // 1. Lưu điểm danh an toàn
    @Transactional
    public SafeCheckin saveCheckin(SafeCheckinDto dto) {
        Shelter shelter = null;
        if (dto.getShelterId() != null) {
            shelter = shelterRepository.findById(dto.getShelterId()).orElse(null);
        }

        SafeCheckin checkin = SafeCheckin.builder()
                .fullName(dto.getFullName())
                .phoneNumber(dto.getPhoneNumber())
                .idCardNumber(dto.getIdCardNumber())
                .originAddress(dto.getOriginAddress())
                .currentShelter(shelter)
                .temporaryAddress(dto.getTemporaryAddress())
                .healthStatus(dto.getHealthStatus() != null ? dto.getHealthStatus() : "Bình thường")
                .build();

        return safeCheckinRepository.save(checkin);
    }

    // 2. Lưu tin tìm kiếm người mất tích + upload ảnh lên Cloudinary
    @Transactional
    public MissingPerson saveMissingReport(MissingPersonDto dto) {
        String photoUrl = null;
        String photoPublicId = null;

        if (dto.getPhotoFile() != null && !dto.getPhotoFile().isEmpty()) {
            try {
                Map<String, Object> uploadResult = cloudinaryService.uploadFile(dto.getPhotoFile(), "missing_persons");
                photoUrl = (String) uploadResult.get("secure_url");
                photoPublicId = (String) uploadResult.get("public_id");
            } catch (Exception e) {
                log.error("Lỗi khi tải ảnh người thân lên Cloudinary: {}", e.getMessage());
            }
        }

        MissingPerson report = MissingPerson.builder()
                .targetName(dto.getTargetName())
                .gender(dto.getGender())
                .age(dto.getAge())
                .lastSeenLocation(dto.getLastSeenLocation())
                .identifyingCharacteristics(dto.getIdentifyingCharacteristics())
                .contactPerson(dto.getContactPerson())
                .contactPhone(dto.getContactPhone())
                .photoUrl(photoUrl)
                .photoPublicId(photoPublicId)
                .status("SEARCHING")
                .build();

        return missingPersonRepository.save(report);
    }

    // 3. Tra cứu danh sách người an toàn
    public List<SafeCheckin> searchSafe(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return safeCheckinRepository.findAllWithShelter();
        }
        return safeCheckinRepository.searchSafePeople(keyword.trim());
    }

    // 4. Tra cứu danh sách người mất tích
    public List<MissingPerson> searchMissing(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return missingPersonRepository.findAllByOrderByCreatedAtDesc();
        }
        return missingPersonRepository.searchMissingPeople(keyword.trim());
    }
}