package com.rescue.disaster.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.rescue.disaster.dto.ProfileUpdateDto;
import com.rescue.disaster.entity.User;
import com.rescue.disaster.repository.UserRepository;
import com.rescue.disaster.service.UserService;

@Controller
@RequestMapping("/user/profile")
public class UserProfileController {

    private final UserRepository userRepository;
    private final UserService userService;

    @Autowired
    public UserProfileController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @GetMapping
    public String showProfile(Authentication auth, Model model) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/auth/login";
        }

        User user = userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin tài khoản!"));

        ProfileUpdateDto dto = new ProfileUpdateDto();
        dto.setFullName(user.getFullName());
        dto.setAddress(user.getAddress());

        model.addAttribute("user", user);
        model.addAttribute("profileDto", dto);
        return "user/profile";
    }

    @PostMapping("/update")
    public String updateProfile(@ModelAttribute("profileDto") ProfileUpdateDto dto,
                                Authentication auth,
                                RedirectAttributes redirectAttributes) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/auth/login";
        }

        try {
            userService.updateProfile(auth.getName(), dto);
            redirectAttributes.addFlashAttribute("successMsg", "Đã cập nhật hồ sơ và ảnh đại diện thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Lỗi cập nhật: " + e.getMessage());
        }

        return "redirect:/user/profile";
    }
}