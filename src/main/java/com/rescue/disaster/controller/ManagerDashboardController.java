package com.rescue.disaster.controller;

import com.rescue.disaster.entity.SosRequest;
import com.rescue.disaster.repository.RescueTeamRepository;
import com.rescue.disaster.repository.SosRequestRepository;
import com.rescue.disaster.service.SosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/manager")
public class ManagerDashboardController {

    private final SosRequestRepository sosRequestRepository;
    private final RescueTeamRepository rescueTeamRepository;
    private final SosService sosService;

    @Autowired
    public ManagerDashboardController(SosRequestRepository sosRequestRepository,
                                      RescueTeamRepository rescueTeamRepository,
                                      SosService sosService) {
        this.sosRequestRepository = sosRequestRepository;
        this.rescueTeamRepository = rescueTeamRepository;
        this.sosService = sosService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("sosList", sosRequestRepository.findAllWithMedia());
        model.addAttribute("teams", rescueTeamRepository.findByStatus("READY"));
        return "manager/dashboard";
    }

    // API lấy chi tiết ca cứu hộ đưa vào Modal
    @GetMapping("/api/sos/{id}")
    @ResponseBody
    public ResponseEntity<?> getSosDetail(@PathVariable("id") Long id) {
        return sosRequestRepository.findByIdWithMedia(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // API cập nhật trạng thái nhiệm vụ
    @PostMapping("/api/sos/{id}/status")
    @ResponseBody
    public ResponseEntity<?> changeStatus(@PathVariable("id") Long id, @RequestBody Map<String, String> body) {
        String newStatus = body.get("status");
        SosRequest updated = sosService.updateSosStatus(id, newStatus);
        return ResponseEntity.ok(Map.of("message", "Đã cập nhật trạng thái", "status", updated.getStatus()));
    }
}