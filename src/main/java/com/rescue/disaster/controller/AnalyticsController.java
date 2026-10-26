package com.rescue.disaster.controller;

import com.rescue.disaster.dto.AnalyticsSummaryDto;
import com.rescue.disaster.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @Autowired
    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    // Giao diện Dashboard thống kê
    @GetMapping("/dashboard")
    public String showAnalyticsDashboard(Model model) {
        model.addAttribute("summary", analyticsService.getSummary());
        return "admin/analytics";
    }

    // API JSON trả dữ liệu biểu đồ cho Chart.js
    @GetMapping("/api/summary")
    @ResponseBody
    public ResponseEntity<AnalyticsSummaryDto> getSummaryApi() {
        return ResponseEntity.ok(analyticsService.getSummary());
    }
}