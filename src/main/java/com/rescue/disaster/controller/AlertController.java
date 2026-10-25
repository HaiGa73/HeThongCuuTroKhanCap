package com.rescue.disaster.controller;

import com.rescue.disaster.dto.EmergencyAlertDto;
import com.rescue.disaster.entity.EmergencyAlert;
import com.rescue.disaster.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AlertController {

    private final AlertService alertService;

    @Autowired
    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    // 1. Màn hình phát cảnh báo khẩn cấp (dành cho Ban chỉ huy / Quản trị)
    @GetMapping("/alerts/broadcast")
    public String showBroadcastCenter(Model model) {
        if (!model.containsAttribute("alertDto")) {
            model.addAttribute("alertDto", new EmergencyAlertDto());
        }
        model.addAttribute("alertHistory", alertService.getAllAlerts());
        return "admin/broadcast-form";
    }

    // Xử lý bắn tin khẩn cấp qua WebSocket
    @PostMapping("/alerts/broadcast")
    public String handleBroadcast(@ModelAttribute("alertDto") EmergencyAlertDto dto,
                                  RedirectAttributes redirectAttributes) {
        alertService.broadcastAlert(dto);
        redirectAttributes.addFlashAttribute("successMsg", "Đã bắn cảnh báo khẩn cấp thành công qua WebSocket đến toàn bộ hệ thống!");
        return "redirect:/alerts/broadcast";
    }

    // 2. Màn hình Cẩm nang sinh tồn thiên tai công khai
    @GetMapping("/survival-guide")
    public String showSurvivalGuide() {
        return "guest/survival-guide";
    }
}