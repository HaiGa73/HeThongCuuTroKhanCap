package com.rescue.disaster.controller;

import com.rescue.disaster.entity.SosRequest;
import com.rescue.disaster.repository.SosRequestRepository;
import com.rescue.disaster.service.SosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/user")
public class UserSosController {

    private final SosRequestRepository sosRequestRepository;
    private final SosService sosService;

    @Autowired
    public UserSosController(SosRequestRepository sosRequestRepository, SosService sosService) {
        this.sosRequestRepository = sosRequestRepository;
        this.sosService = sosService;
    }

    // 1. Hiển thị danh sách các ca cứu nạn do tài khoản hiện tại tạo
    @GetMapping("/my-sos")
    public String showMySos(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/auth/login";
        }

        String username = authentication.getName();

        // Lấy danh sách ca SOS thuộc về người dùng hiện tại và sắp xếp mới nhất lên đầu
        List<SosRequest> myList = sosRequestRepository.findAll().stream()
                .filter(s -> s.getUser() != null && username.equals(s.getUser().getUsername()))
                .sorted((a, b) -> b.getSosId().compareTo(a.getSosId()))
                .toList();

        model.addAttribute("mySosList", myList);
        return "user/my-sos";
    }

    // 2. Cho phép người dân hủy ca nếu nước đã rút hoặc đã tự thoát an toàn
    @PostMapping("/my-sos/{id}/cancel")
    public String cancelMySos(@PathVariable("id") Long id,
                             @RequestParam("cancelReason") String cancelReason,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/auth/login";
        }

        try {
            sosService.cancelSosByUser(id, authentication.getName(), cancelReason);
            redirectAttributes.addFlashAttribute("successMsg", "Đã gửi yêu cầu hủy ca cứu nạn thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Không thể hủy: " + e.getMessage());
        }

        return "redirect:/user/my-sos";
    }
}