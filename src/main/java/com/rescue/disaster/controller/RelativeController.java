package com.rescue.disaster.controller;

import com.rescue.disaster.dto.MissingPersonDto;
import com.rescue.disaster.dto.SafeCheckinDto;
import com.rescue.disaster.repository.ShelterRepository;
import com.rescue.disaster.service.RelativeSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/relatives")
public class RelativeController {

    private final RelativeSearchService relativeService;
    private final ShelterRepository shelterRepository;

    @Autowired
    public RelativeController(RelativeSearchService relativeService, ShelterRepository shelterRepository) {
        this.relativeService = relativeService;
        this.shelterRepository = shelterRepository;
    }

    // 1. Cổng tra cứu tìm kiếm tập trung
    @GetMapping("/search")
    public String showSearchPortal(@RequestParam(value = "keyword", required = false) String keyword,
                                   @RequestParam(value = "tab", defaultValue = "safe") String tab,
                                   Model model) {
        model.addAttribute("keyword", keyword);
        model.addAttribute("activeTab", tab);
        model.addAttribute("safeList", relativeService.searchSafe(keyword));
        model.addAttribute("missingList", relativeService.searchMissing(keyword));
        return "guest/relatives-search";
    }

    // 2. Giao diện điểm danh an toàn
    @GetMapping("/checkin")
    public String showCheckinForm(Model model) {
        if (!model.containsAttribute("checkinDto")) {
            model.addAttribute("checkinDto", new SafeCheckinDto());
        }
        model.addAttribute("shelters", shelterRepository.findByIsActiveTrue());
        return "guest/checkin-form";
    }

    // Xử lý gửi báo an
    @PostMapping("/checkin")
    public String handleCheckin(@ModelAttribute("checkinDto") SafeCheckinDto dto,
                                RedirectAttributes redirectAttributes) {
        relativeService.saveCheckin(dto);
        redirectAttributes.addFlashAttribute("successMsg", "Khai báo an toàn thành công! Người thân đã có thể tra cứu thấy bạn.");
        return "redirect:/relatives/search?tab=safe";
    }

    // 3. Giao diện đăng tin tìm người
    @GetMapping("/missing/report")
    public String showMissingReportForm(Model model) {
        if (!model.containsAttribute("missingDto")) {
            model.addAttribute("missingDto", new MissingPersonDto());
        }
        return "guest/missing-report-form";
    }

    // Xử lý gửi tin tìm kiếm
    @PostMapping("/missing/report")
    public String handleMissingReport(@ModelAttribute("missingDto") MissingPersonDto dto,
                                      RedirectAttributes redirectAttributes) {
        relativeService.saveMissingReport(dto);
        redirectAttributes.addFlashAttribute("successMsg", "Đã đăng tải tin tìm kiếm người thân lên hệ thống ứng cứu!");
        return "redirect:/relatives/search?tab=missing";
    }
}