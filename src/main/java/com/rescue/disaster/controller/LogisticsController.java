package com.rescue.disaster.controller;

import com.rescue.disaster.dto.ReliefDonationDto;
import com.rescue.disaster.entity.Warehouse;
import com.rescue.disaster.repository.ShelterRepository;
import com.rescue.disaster.service.LogisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/logistics")
public class LogisticsController {

    private final LogisticsService logisticsService;
    private final ShelterRepository shelterRepository;

    @Autowired
    public LogisticsController(LogisticsService logisticsService, ShelterRepository shelterRepository) {
        this.logisticsService = logisticsService;
        this.shelterRepository = shelterRepository;
    }

    // 1. Quản lý kho hàng & nhập xuất tồn dã chiến
    @GetMapping("/inventory")
    public String showInventoryDashboard(@RequestParam(value = "warehouseId", required = false) Long warehouseId,
                                         Model model) {
        List<Warehouse> warehouses = logisticsService.getAllWarehouses();
        Long selectedId = (warehouseId != null && !warehouses.isEmpty()) ? warehouseId : (warehouses.isEmpty() ? null : warehouses.get(0).getWarehouseId());

        model.addAttribute("warehouses", warehouses);
        model.addAttribute("selectedWarehouseId", selectedId);
        model.addAttribute("supplies", logisticsService.getAllSupplies());

        if (selectedId != null) {
            model.addAttribute("inventories", logisticsService.getInventoriesByWarehouse(selectedId));
        }
        return "logistics/inventory";
    }

    // API xử lý nhập/xuất kho nhanh không cần tải lại trang
    @PostMapping("/api/stock-adjust")
    @ResponseBody
    public ResponseEntity<?> adjustStockApi(@RequestBody Map<String, Object> payload) {
        try {
            Long warehouseId = Long.valueOf(payload.get("warehouseId").toString());
            Integer supplyId = Integer.valueOf(payload.get("supplyId").toString());
            int changeAmount = Integer.parseInt(payload.get("amount").toString());

            var updated = logisticsService.adjustStock(warehouseId, supplyId, changeAmount);
            return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "newQuantity", updated.getQuantity(),
                    "message", (changeAmount > 0 ? "Đã nhập thêm " : "Đã xuất kho ") + Math.abs(changeAmount)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("status", "ERROR", "message", e.getMessage()));
        }
    }

    // 2. Màn hình nhà hảo tâm & đoàn tình nguyện đăng ký ủng hộ
    @GetMapping("/donate")
    public String showDonationForm(Model model) {
        if (!model.containsAttribute("donationDto")) {
            model.addAttribute("donationDto", new ReliefDonationDto());
        }
        model.addAttribute("shelters", shelterRepository.findByIsActiveTrue());
        model.addAttribute("donations", logisticsService.getAllDonations());
        return "logistics/donate";
    }

    // Tiếp nhận form đăng ký ủng hộ
    @PostMapping("/donate")
    public String handleDonationSubmit(@ModelAttribute("donationDto") ReliefDonationDto dto,
                                       RedirectAttributes redirectAttributes) {
        logisticsService.registerDonation(dto);
        redirectAttributes.addFlashAttribute("successMsg", "Đăng ký tiếp tế thành công! Đội điều phối sẽ liên hệ để hướng dẫn lộ trình tiếp cận an toàn.");
        return "redirect:/logistics/donate";
    }
}