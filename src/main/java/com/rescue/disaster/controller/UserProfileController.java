package com.rescue.disaster.controller;

import com.rescue.disaster.dto.ChangePasswordDto;
import com.rescue.disaster.dto.ProfileUpdateDto;
import com.rescue.disaster.entity.User;
import com.rescue.disaster.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user/profile")
public class UserProfileController {

    private final UserService userService;

    @Autowired
    public UserProfileController(UserService userService) {
        this.userService = userService;
    }

    // Hiển thị trang hồ sơ cá nhân
    @GetMapping
    public String showProfilePage(Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userService.getUserByUsername(username);

        ProfileUpdateDto profileDto = new ProfileUpdateDto();
        profileDto.setFullName(user.getFullName());
        profileDto.setPhoneNumber(user.getPhoneNumber());
        profileDto.setEmail(user.getEmail());

        model.addAttribute("user", user);
        model.addAttribute("profileDto", profileDto);
        if (!model.containsAttribute("passwordDto")) {
            model.addAttribute("passwordDto", new ChangePasswordDto());
        }

        return "user/profile";
    }

    // Cập nhật thông tin cá nhân & Avatar
    @PostMapping("/update")
    public String handleUpdateProfile(@ModelAttribute("profileDto") ProfileUpdateDto dto,
                                      Authentication authentication,
                                      RedirectAttributes redirectAttributes) {
        try {
            userService.updateProfile(authentication.getName(), dto);
            redirectAttributes.addFlashAttribute("profileSuccessMsg", "Cập nhật hồ sơ cá nhân thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("profileErrorMsg", e.getMessage());
        }
        return "redirect:/user/profile";
    }

    // Đổi mật khẩu
    @PostMapping("/change-password")
    public String handleChangePassword(@ModelAttribute("passwordDto") ChangePasswordDto dto,
                                       Authentication authentication,
                                       RedirectAttributes redirectAttributes) {
        try {
            userService.changePassword(authentication.getName(), dto);
            redirectAttributes.addFlashAttribute("pwdSuccessMsg", "Đổi mật khẩu thành công! Mật khẩu mới đã có hiệu lực.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("pwdErrorMsg", e.getMessage());
        }
        return "redirect:/user/profile";
    }
}