package com.rescue.disaster.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.rescue.disaster.entity.RescueTeam;
import com.rescue.disaster.entity.SosRequest;
import com.rescue.disaster.entity.User;
import com.rescue.disaster.repository.RescueTeamRepository;
import com.rescue.disaster.repository.SosRequestRepository;
import com.rescue.disaster.repository.UserRepository;
import com.rescue.disaster.service.UserService;

@Controller
@RequestMapping("/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final RescueTeamRepository rescueTeamRepository;
    private final SosRequestRepository sosRequestRepository;

    @Autowired
    public AdminUserController(UserService userService,
                               UserRepository userRepository,
                               RescueTeamRepository rescueTeamRepository,
                               SosRequestRepository sosRequestRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.rescueTeamRepository = rescueTeamRepository;
        this.sosRequestRepository = sosRequestRepository;
    }

    // 1. Danh sách người dùng
    @GetMapping
    public String listUsers(@RequestParam(value = "keyword", required = false) String keyword,
                            @RequestParam(value = "role", required = false) String role,
                            Model model) {
        List<User> users = userService.searchUsers(keyword, role);
        model.addAttribute("users", users);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedRole", role);
        return "admin/users";
    }

    // 2. Đổi vai trò tài khoản
    @PostMapping("/{id}/role")
    public String changeRole(@PathVariable("id") Long id,
                             @RequestParam("role") String role,
                             RedirectAttributes redirectAttributes) {
        try {
            userService.updateUserRole(id, role);
            redirectAttributes.addFlashAttribute("successMsg", "Đã cập nhật vai trò người dùng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // 3. Khóa / Mở khóa tài khoản
    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(@PathVariable("id") Long id,
                               RedirectAttributes redirectAttributes) {
        try {
            userService.toggleUserStatus(id);
            redirectAttributes.addFlashAttribute("successMsg", "Đã thay đổi trạng thái kích hoạt tài khoản!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Lỗi: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    // 4. TRANG QUẢN LÝ CÁC ĐỘI CỦA MANAGER
    @GetMapping("/manager/{id}/teams")
    public String viewManagerTeams(@PathVariable("id") Long managerId, Model model) {
        User manager = userRepository.findById(managerId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Manager ID: " + managerId));

        List<RescueTeam> teams = rescueTeamRepository.findByManager_UserId(managerId);
        model.addAttribute("manager", manager);
        model.addAttribute("managerId", managerId); 
        model.addAttribute("teams", teams);
        return "admin/manager-teams";
    }

    // 4.1 Thêm đội mới cho Manager
    @PostMapping("/manager/{id}/teams/add")
    public String addTeamToManager(@PathVariable("id") Long managerId,
                                   @RequestParam("teamName") String teamName,
                                   @RequestParam("leaderName") String leaderName,
                                   @RequestParam("contactPhone") String contactPhone,
                                   RedirectAttributes redirectAttributes) {
        try {
            User manager = userRepository.findById(managerId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy Manager!"));

            RescueTeam team = new RescueTeam();
            team.setTeamName(teamName);
            team.setLeaderName(leaderName);
            team.setContactPhone(contactPhone);
            team.setStatus("READY");
            team.setManager(manager);

            rescueTeamRepository.save(team);
            redirectAttributes.addFlashAttribute("successMsg", "Đã thêm đội mới thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Lỗi thêm đội: " + e.getMessage());
        }
        return "redirect:/admin/users/manager/" + managerId + "/teams";
    }

    // 4.2 Sửa thông tin đội
    @PostMapping("/manager/{id}/teams/{teamId}/update")
    public String updateTeam(@PathVariable("id") Long managerId,
                             @PathVariable("teamId") Long teamId,
                             @RequestParam("teamName") String teamName,
                             @RequestParam("leaderName") String leaderName,
                             @RequestParam("contactPhone") String contactPhone,
                             @RequestParam("status") String status,
                             RedirectAttributes redirectAttributes) {
        try {
            RescueTeam team = rescueTeamRepository.findById(teamId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đội cứu hộ!"));

            team.setTeamName(teamName);
            team.setLeaderName(leaderName);
            team.setContactPhone(contactPhone);
            team.setStatus(status);

            rescueTeamRepository.save(team);
            redirectAttributes.addFlashAttribute("successMsg", "Đã cập nhật thông tin đội thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Lỗi sửa đội: " + e.getMessage());
        }
        return "redirect:/admin/users/manager/" + managerId + "/teams";
    }

    // 4.3 Xóa đội
    @PostMapping("/manager/{id}/teams/{teamId}/delete")
    public String deleteTeam(@PathVariable("id") Long managerId,
                             @PathVariable("teamId") Long teamId,
                             RedirectAttributes redirectAttributes) {
        try {
            rescueTeamRepository.deleteById(teamId);
            redirectAttributes.addFlashAttribute("successMsg", "Đã xóa đội cứu nạn thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Lỗi xóa đội: " + e.getMessage());
        }
        return "redirect:/admin/users/manager/" + managerId + "/teams";
    }

    // 5. TRANG CHI TIẾT VÀ CHỈNH SỬA TÀI KHOẢN NGƯỜI DÙNG (USER)
    @GetMapping("/{id}/detail")
    public String userDetail(@PathVariable("id") Long userId, Model model) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng ID: " + userId));

        // Lọc an toàn các ca SOS của người dùng này
        List<SosRequest> userSosList = sosRequestRepository.findAll().stream()
                .filter(s -> s.getUser() != null && userId.equals(s.getUser().getUserId()))
                .sorted((a, b) -> b.getSosId().compareTo(a.getSosId()))
                .toList();

        model.addAttribute("user", user);
        model.addAttribute("sosList", userSosList);
        return "admin/user-detail";
    }

    // 5.1 Cập nhật thông tin User
    @PostMapping("/{id}/detail/update")
    public String updateUserDetail(@PathVariable("id") Long userId,
                                   @RequestParam("fullName") String fullName,
                                   @RequestParam("email") String email,
                                   @RequestParam("phoneNumber") String phoneNumber,
                                   RedirectAttributes redirectAttributes) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng!"));

            user.setFullName(fullName);
            user.setEmail(email);
            user.setPhoneNumber(phoneNumber);
            userRepository.save(user);

            redirectAttributes.addFlashAttribute("successMsg", "Đã cập nhật hồ sơ người dùng thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Lỗi cập nhật: " + e.getMessage());
        }
        return "redirect:/admin/users/" + userId + "/detail";
    }
}