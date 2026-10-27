package com.rescue.disaster.controller;

import com.rescue.disaster.dto.MissingPersonDto;
import com.rescue.disaster.dto.SafeCheckinDto;
import com.rescue.disaster.entity.MissingPerson;
import com.rescue.disaster.entity.SafeCheckin;
import com.rescue.disaster.repository.ShelterRepository;
import com.rescue.disaster.service.RelativeSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/relatives")
public class RelativeController {

    private final RelativeSearchService relativeSearchService;
    private final ShelterRepository shelterRepository;

    @Autowired
    public RelativeController(RelativeSearchService relativeSearchService,
                              ShelterRepository shelterRepository) {
        this.relativeSearchService = relativeSearchService;
        this.shelterRepository = shelterRepository;
    }

    // 1. Cổng tra cứu tìm kiếm thân nhân
    @GetMapping("/search")
    public String searchRelatives(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        // Tận dụng 2 hàm có sẵn trong RelativeSearchService
        List<SafeCheckin> safeList = relativeSearchService.searchSafe(keyword);
        List<MissingPerson> missingList = relativeSearchService.searchMissing(keyword);

        model.addAttribute("safeCheckins", safeList);
        model.addAttribute("missingReports", missingList);
        model.addAttribute("keyword", keyword);
        return "guest/relatives-search";
    }

    // 2. Form điểm danh an toàn
    @GetMapping("/checkin")
    public String showCheckinForm(Model model) {
        SafeCheckinDto dto = new SafeCheckinDto();
        model.addAttribute("checkinDto", dto);
        model.addAttribute("safeCheckinDto", dto);
        model.addAttribute("shelters", shelterRepository.findAll());
        return "guest/checkin-form";
    }

    @PostMapping("/checkin")
    public String handleCheckin(@ModelAttribute("checkinDto") SafeCheckinDto dto, RedirectAttributes redirectAttributes) {
        try {
            // Gọi hàm saveCheckin có sẵn trong RelativeSearchService
            relativeSearchService.saveCheckin(dto);
            redirectAttributes.addFlashAttribute("successMsg", "Điểm danh an toàn thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Lỗi: " + e.getMessage());
        }
        return "redirect:/relatives/search";
    }

    // 3. Form đăng tin tìm người mất tích
    @GetMapping("/missing/report")
    public String showMissingReportForm(Model model) {
        MissingPersonDto dto = new MissingPersonDto();
        model.addAttribute("missingDto", dto);
        model.addAttribute("missingPersonDto", dto);
        return "guest/missing-report-form";
    }

    @PostMapping("/missing/report")
    public String handleMissingReport(@ModelAttribute("missingDto") MissingPersonDto dto, RedirectAttributes redirectAttributes) {
        try {
            // Gọi hàm saveMissingReport có sẵn trong RelativeSearchService
            relativeSearchService.saveMissingReport(dto);
            redirectAttributes.addFlashAttribute("successMsg", "Đã đăng tin tìm kiếm thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Lỗi khi lưu tin: " + e.getMessage());
        }
        return "redirect:/relatives/search";
    }
}