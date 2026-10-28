package com.rescue.disaster.controller;

import com.rescue.disaster.entity.RescueTeam;
import com.rescue.disaster.entity.SosRequest;
import com.rescue.disaster.entity.User;
import com.rescue.disaster.repository.RescueTeamRepository;
import com.rescue.disaster.repository.SosRequestRepository;
import com.rescue.disaster.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/team")
@PreAuthorize("hasAnyRole('RESCUE_TEAM', 'ADMIN')")
public class TeamMissionController {

    private final UserRepository userRepository;
    private final RescueTeamRepository rescueTeamRepository;
    private final SosRequestRepository sosRequestRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Autowired
    public TeamMissionController(UserRepository userRepository,
                                 RescueTeamRepository rescueTeamRepository,
                                 SosRequestRepository sosRequestRepository,
                                 SimpMessagingTemplate messagingTemplate) {
        this.userRepository = userRepository;
        this.rescueTeamRepository = rescueTeamRepository;
        this.sosRequestRepository = sosRequestRepository;
        this.messagingTemplate = messagingTemplate;
    }

    // 1. Màn hình tác chiến duy nhất của Đội cứu hộ
    @GetMapping("/mission")
    public String showMission(Authentication auth, Model model) {
        if (auth == null) return "redirect:/auth/login";

        User user = userRepository.findByUsername(auth.getName()).orElse(null);
        if (user == null) return "redirect:/auth/login";

        // Tìm thông tin đội tương ứng qua SĐT liên hệ
        RescueTeam team = rescueTeamRepository.findByContactPhone(user.getPhoneNumber())
                .orElse(null);

        // Nếu là ADMIN test thử hoặc chưa khớp SĐT thì lấy đội đầu tiên
        if (team == null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("ADMIN"))) {
            team = rescueTeamRepository.findAll().stream().findFirst().orElse(null);
        }

        if (team == null) {
            model.addAttribute("error", "Tài khoản của bạn chưa được liên kết với hồ sơ Đội cứu hộ nào!");
            return "team/mission";
        }

        List<SosRequest> myMissions = sosRequestRepository.findByRescueTeam_TeamIdOrderByCreatedAtDesc(team.getTeamId());

        model.addAttribute("team", team);
        model.addAttribute("missions", myMissions);
        return "team/mission";
    }

    // 2. Thao tác duy nhất: Bấm "XÁC NHẬN NHẬN LỆNH & XUẤT PHÁT"
    @PostMapping("/mission/{id}/confirm")
    public String confirmMission(@PathVariable("id") Long sosId,
                                 RedirectAttributes redirectAttributes) {
        SosRequest sos = sosRequestRepository.findById(sosId).orElse(null);
        if (sos != null) {
            sos.setStatus("IN_PROGRESS");
            String oldNote = sos.getManagerNote() != null ? sos.getManagerNote() : "";
            sos.setManagerNote(oldNote + " | [ĐỘI ĐÃ XÁC NHẬN LỆNH & ĐANG TIẾP CẬN MỤC TIÊU]");
            sosRequestRepository.save(sos);

            // Báo lại trạng thái cho Manager và Người dân
            try {
                messagingTemplate.convertAndSend("/topic/manager/sos-status-updated", sos);
            } catch (Exception ignored) {}

            redirectAttributes.addFlashAttribute("successMsg", "Đã xác nhận nhận lệnh tác chiến thành công!");
        }
        return "redirect:/team/mission";
    }

    // 3. Thao tác phụ: Bấm "ĐÃ HOÀN THÀNH GIẢI CỨU" (Khi đưa dân về nơi an toàn)
    @PostMapping("/mission/{id}/complete")
    public String completeMission(@PathVariable("id") Long sosId,
                                  RedirectAttributes redirectAttributes) {
        SosRequest sos = sosRequestRepository.findById(sosId).orElse(null);
        if (sos != null) {
            sos.setStatus("COMPLETED");
            sosRequestRepository.save(sos);

            try {
                messagingTemplate.convertAndSend("/topic/manager/sos-status-updated", sos);
            } catch (Exception ignored) {}

            redirectAttributes.addFlashAttribute("successMsg", "Đã hoàn thành xuất sắc ca cứu nạn!");
        }
        return "redirect:/team/mission";
    }
}