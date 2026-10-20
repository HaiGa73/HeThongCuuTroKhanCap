package com.rescue.disaster.controller;

import com.rescue.disaster.dto.SosCreateRequestDto;
import com.rescue.disaster.service.SosService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/sos")
@RequiredArgsConstructor
public class SosController {

    private final SosService sosService;

    @GetMapping
    public String showSosForm(Model model) {
        model.addAttribute("sosDto", new SosCreateRequestDto());
        return "guest/sos-form";
    }

    @PostMapping("/send")
    public String handleSendSos(@ModelAttribute SosCreateRequestDto dto, Model model) {
        sosService.createSos(dto);
        model.addAttribute("successMsg", "Tín hiệu SOS của bạn đã được chuyển tới Đội cứu nạn gần nhất!");
        return "guest/sos-form";
    }
}