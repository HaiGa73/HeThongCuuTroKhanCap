package com.rescue.disaster.controller;

import com.rescue.disaster.entity.RescueTeam;
import com.rescue.disaster.entity.SosRequest;
import com.rescue.disaster.repository.SosRequestRepository;
import com.rescue.disaster.service.SosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequestMapping("/manager")
public class ManagerDashboardController {

    private final SosRequestRepository sosRequestRepository;
    private final SosService sosService;

    @Autowired
    public ManagerDashboardController(SosRequestRepository sosRequestRepository, SosService sosService) {
        this.sosRequestRepository = sosRequestRepository;
        this.sosService = sosService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("sosList", sosRequestRepository.findAllWithMedia());
        return "manager/dashboard";
    }

    // 1. API lấy chi tiết ca cứu nạn kèm danh sách các đội mà Manager hiện tại ĐƯỢC PHÉP ĐIỀU ĐỘNG
    @GetMapping("/api/sos/{id}")
    @ResponseBody
    @org.springframework.transaction.annotation.Transactional(readOnly = true)
    public ResponseEntity<?> getSosDetail(@PathVariable("id") Long id, Authentication auth) {
        String username = auth != null ? auth.getName() : "";
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("ADMIN"));

        List<RescueTeam> myTeams = sosService.getTeamsForUser(username, isAdmin);

        return sosRequestRepository.findById(id).map(sos -> {
            Map<String, Object> res = new HashMap<>();
            res.put("sosId", sos.getSosId());
            res.put("senderName", sos.getSenderName());
            res.put("phoneNumber", sos.getPhoneNumber());
            res.put("addressDetail", sos.getAddressDetail());
            res.put("numberOfPeople", sos.getNumberOfPeople());
            res.put("latitude", sos.getLatitude());
            res.put("longitude", sos.getLongitude());
            res.put("description", sos.getDescription());
            res.put("status", sos.getStatus());
            res.put("managerNote", sos.getManagerNote());
            res.put("assignedTeamId", sos.getRescueTeam() != null ? sos.getRescueTeam().getTeamId() : null);

            // Danh sách ảnh
            List<Map<String, String>> mediaData = new ArrayList<>();
            if (sos.getMediaList() != null) {
                for (com.rescue.disaster.entity.SosMedia m : sos.getMediaList()) {
                    mediaData.add(Map.of("mediaUrl", m.getMediaUrl()));
                }
            }
            res.put("mediaList", mediaData);

            // Danh sách các đội cứu hộ mà Manager này có quyền chọn
            List<Map<String, Object>> teamList = new ArrayList<>();
            for (RescueTeam t : myTeams) {
                Map<String, Object> tMap = new HashMap<>();
                tMap.put("teamId", t.getTeamId());
                tMap.put("teamName", t.getTeamName());
                teamList.add(tMap);
            }
            res.put("availableTeams", teamList);

            return ResponseEntity.ok(res);
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    // 2. API cập nhật phương án xử lý & gán đội
    @PostMapping("/api/sos/{id}/status")
    @ResponseBody
    public ResponseEntity<?> changeStatus(@PathVariable("id") Long id, 
                                         @RequestBody Map<String, Object> body,
                                         Authentication auth) {
        String username = auth != null ? auth.getName() : "";
        boolean isAdmin = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("ADMIN"));

        String newStatus = (String) body.get("status");
        String note = (String) body.get("note");
        Long teamId = body.get("teamId") != null && !body.get("teamId").toString().isBlank()
                ? Long.valueOf(body.get("teamId").toString())
                : null;

        SosRequest updated = sosService.updateSosHandling(id, newStatus, note, teamId, username, isAdmin);
        
        return ResponseEntity.ok(Map.of(
                "message", "Đã cập nhật phương án và phân công đội thành công",
                "status", updated.getStatus(),
                "managerNote", updated.getManagerNote() != null ? updated.getManagerNote() : ""
        ));
    }
}