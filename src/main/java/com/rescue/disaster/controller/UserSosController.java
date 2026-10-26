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

    // Hiển thị danh sách các ca cứu nạn của cá nhân
    @GetMapping("/my-sos")
    public String showMySosList(Authentication authentication, Model model) {
        String username = authentication.getName();
        List<SosRequest> myRequests = sosRequestRepository.findAllByUsernameWithMedia(username);
        model.addAttribute("myRequests", myRequests);
        model.addAttribute("username", username);
        return "user/my-sos";
    }

    // Xử lý hủy ca SOS
    @PostMapping("/my-sos/{id}/cancel")
    public String cancelMySos(@PathVariable("id") Long id,
                             @RequestParam(value = "reason", defaultValue = "Tôi đã tự sơ tán an toàn") String reason,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            sosService.cancelSosByUser(id, authentication.getName(), reason);
            redirectAttributes.addFlashAttribute("successMsg", "Đã hủy ca cứu nạn thành công. Cảm ơn bạn đã phản hồi để nhường lực lượng cứu trợ cho các trường hợp khác!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Không thể hủy ca: " + e.getMessage());
        }
        return "redirect:/user/my-sos";
    }
}