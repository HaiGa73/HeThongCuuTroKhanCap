package com.rescue.disaster.controller;

import com.rescue.disaster.entity.EmergencyAlert;
import com.rescue.disaster.repository.EmergencyAlertRepository;
import com.rescue.disaster.repository.ShelterRepository;
import com.rescue.disaster.repository.SosRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    private final SosRequestRepository sosRequestRepository;
    private final ShelterRepository shelterRepository;
    private final EmergencyAlertRepository emergencyAlertRepository;

    @Autowired
    public HomeController(SosRequestRepository sosRequestRepository,
                          ShelterRepository shelterRepository,
                          EmergencyAlertRepository emergencyAlertRepository) {
        this.sosRequestRepository = sosRequestRepository;
        this.shelterRepository = shelterRepository;
        this.emergencyAlertRepository = emergencyAlertRepository;
    }

    @GetMapping("/")
    public String showHomePage(Authentication authentication, Model model) {
        // 1. Nhận diện trạng thái đăng nhập và phân quyền
        boolean isAuthenticated = (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName()));
        model.addAttribute("isAuthenticated", isAuthenticated);

        if (isAuthenticated) {
            model.addAttribute("currentUsername", authentication.getName());
            boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isManager = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));
            model.addAttribute("isAdmin", isAdmin);
            model.addAttribute("isManager", isManager);
        }

        // 2. Chỉ số KPI nhanh hiển thị trên Banner
        model.addAttribute("totalSos", sosRequestRepository.countByIsDeletedFalse());
        model.addAttribute("completedSos", sosRequestRepository.countByStatusAndIsDeletedFalse("COMPLETED"));
        model.addAttribute("activeShelters", shelterRepository.findByIsActiveTrue().size());

        // 3. Danh sách các bản tin cảnh báo khẩn cấp mới nhất
        List<EmergencyAlert> alerts = emergencyAlertRepository.findAllByOrderByCreatedAtDesc();
        model.addAttribute("latestAlerts", alerts.stream().limit(3).toList());

        return "index";
    }
}