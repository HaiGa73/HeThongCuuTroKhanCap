package com.rescue.disaster.controller;

import com.rescue.disaster.dto.EmergencyAlertDto;
import com.rescue.disaster.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Collections;

@Controller
public class AlertController {

    private final AlertService alertService;

    @Autowired
    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    // 1. Màn hình phát cảnh báo khẩn cấp (Hỗ trợ cả 2 URL để không bị lệch link)
    @GetMapping({"/alerts/broadcast", "/admin/broadcast"})
    public String showBroadcastCenter(Model model) {
        if (!model.containsAttribute("alertDto")) {
            model.addAttribute("alertDto", new EmergencyAlertDto());
        }
        try {
            model.addAttribute("alertHistory", alertService.getAllAlerts());
        } catch (Exception e) {
            model.addAttribute("alertHistory", Collections.emptyList());
        }
        return "admin/broadcast-form";
    }

    // Xử lý phát tín hiệu khẩn cấp qua WebSocket + Database
    @PostMapping({"/alerts/broadcast", "/admin/broadcast"})
    public String handleBroadcast(@ModelAttribute("alertDto") EmergencyAlertDto dto,
                                  RedirectAttributes redirectAttributes) {
        try {
            alertService.broadcastAlert(dto);
            redirectAttributes.addFlashAttribute("successMsg", "Đã bắn cảnh báo khẩn cấp thành công đến toàn bộ hệ thống!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Không thể phát cảnh báo: " + e.getMessage());
        }
        return "redirect:/alerts/broadcast";
    }

    // 2. Màn hình Cẩm nang sinh tồn thiên tai công khai
    @GetMapping("/survival-guide")
    public String showSurvivalGuide() {
        return "guest/survival-guide";
    }
}