package com.rescue.disaster.controller;

import com.rescue.disaster.dto.SosCreateRequestDto;
import com.rescue.disaster.service.SosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/sos")
public class SosController {

    private final SosService sosService;

    @Autowired
    public SosController(SosService sosService) {
        this.sosService = sosService;
    }

    @GetMapping
    public String showSosForm(Model model) {
        if (!model.containsAttribute("sosDto")) {
            model.addAttribute("sosDto", new SosCreateRequestDto());
        }
        return "guest/sos-form";
    }

    // Nhận cả POST /sos và POST /sos/send
    @PostMapping({"", "/send"})
    public String handleSendSos(@ModelAttribute("sosDto") SosCreateRequestDto dto,
                                RedirectAttributes redirectAttributes) {
        sosService.createSos(dto);
        redirectAttributes.addFlashAttribute("successMsg", "Tín hiệu SOS của bạn đã được chuyển tới Đội cứu nạn gần nhất!");
        return "redirect:/sos";
    }
}