package com.rescue.disaster.controller;

import com.rescue.disaster.repository.SosRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/manager")
@RequiredArgsConstructor
public class ManagerDashboardController {

    private final SosRequestRepository sosRequestRepository;

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        // Tải sẵn danh sách các ca SOS chưa bị xóa mềm từ database lên bảng
        model.addAttribute("sosList", sosRequestRepository.findByIsDeletedFalseOrderByCreatedAtDesc());
        return "manager/dashboard";
    }
}