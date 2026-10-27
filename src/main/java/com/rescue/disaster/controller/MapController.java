package com.rescue.disaster.controller;

import com.rescue.disaster.entity.Shelter;
import com.rescue.disaster.entity.SosRequest;
import com.rescue.disaster.repository.ShelterRepository;
import com.rescue.disaster.repository.SosRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
public class MapController {

    private final ShelterRepository shelterRepository;
    private final SosRequestRepository sosRequestRepository;

    @Autowired
    public MapController(ShelterRepository shelterRepository, SosRequestRepository sosRequestRepository) {
        this.shelterRepository = shelterRepository;
        this.sosRequestRepository = sosRequestRepository;
    }

    @GetMapping("/map")
    public String showPublicMap(Authentication authentication, Model model) {
        boolean isManagerOrAdmin = false;
        if (authentication != null && authentication.isAuthenticated()) {
            isManagerOrAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER") || a.getAuthority().equals("ROLE_ADMIN"));
        }
        model.addAttribute("isManagerOrAdmin", isManagerOrAdmin);
        return "guest/map";
    }

    // 1. API công khai trả về danh sách các Điểm sơ tán trú ẩn
    @GetMapping("/api/map/shelters")
    @ResponseBody
    public ResponseEntity<?> getShelters() {
        List<Shelter> list = shelterRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (Shelter s : list) {
            if (s.getLatitude() != null && s.getLongitude() != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("shelterId", s.getShelterId());
                map.put("name", s.getName());
                map.put("address", s.getAddress());
                map.put("latitude", s.getLatitude());
                map.put("longitude", s.getLongitude());
                map.put("capacity", s.getCapacity() != null ? s.getCapacity() : 100);
                map.put("contactPhone", s.getContactPhone() != null ? s.getContactPhone() : "112");
                result.add(map);
            }
        }
        return ResponseEntity.ok(result);
    }

    // 2. API trả về các ca kêu cứu SOS thực địa
    @GetMapping("/api/map/sos-locations")
    @ResponseBody
    public ResponseEntity<?> getSosLocations() {
        List<SosRequest> list = sosRequestRepository.findAll();
        List<Map<String, Object>> result = new ArrayList<>();

        for (SosRequest s : list) {
            if (s.getLatitude() != null && s.getLongitude() != null 
                    && !"COMPLETED".equalsIgnoreCase(s.getStatus()) 
                    && !"CANCELLED".equalsIgnoreCase(s.getStatus())) {
                Map<String, Object> map = new HashMap<>();
                map.put("sosId", s.getSosId());
                map.put("senderName", s.getSenderName() != null ? s.getSenderName() : "Người dân");
                map.put("phoneNumber", s.getPhoneNumber() != null ? s.getPhoneNumber() : "Chưa rõ");
                map.put("latitude", s.getLatitude());
                map.put("longitude", s.getLongitude());
                map.put("priority", s.getPriority() != null ? s.getPriority() : "HIGH");
                map.put("numberOfPeople", s.getNumberOfPeople() != null ? s.getNumberOfPeople() : 1);
                map.put("addressDetail", s.getAddressDetail() != null ? s.getAddressDetail() : "Chưa rõ địa chỉ");
                result.add(map);
            }
        }
        return ResponseEntity.ok(result);
    }
}