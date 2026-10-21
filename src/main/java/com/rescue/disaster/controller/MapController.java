package com.rescue.disaster.controller;

import com.rescue.disaster.entity.Shelter;
import com.rescue.disaster.entity.SosRequest;
import com.rescue.disaster.repository.ShelterRepository;
import com.rescue.disaster.repository.SosRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class MapController {

    private final SosRequestRepository sosRequestRepository;
    private final ShelterRepository shelterRepository;

    @Autowired
    public MapController(SosRequestRepository sosRequestRepository, ShelterRepository shelterRepository) {
        this.sosRequestRepository = sosRequestRepository;
        this.shelterRepository = shelterRepository;
    }

    @GetMapping("/map")
    public String showPublicMap() {
        return "guest/map";
    }

    // API lấy toàn bộ ca SOS chưa bị xóa mềm
    @GetMapping("/api/map/sos-points")
    @ResponseBody
    public ResponseEntity<List<SosRequest>> getSosPoints() {
        List<SosRequest> list = sosRequestRepository.findAllWithMedia();
        return ResponseEntity.ok(list);
    }

    // API lấy danh sách các điểm sơ tán và trạm y tế
    @GetMapping("/api/map/shelters")
    @ResponseBody
    public ResponseEntity<List<Shelter>> getShelters() {
        List<Shelter> shelters = shelterRepository.findByIsActiveTrue();
        return ResponseEntity.ok(shelters);
    }
}