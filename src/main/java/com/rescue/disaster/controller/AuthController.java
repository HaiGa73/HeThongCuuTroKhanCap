package com.rescue.disaster.controller;

import com.rescue.disaster.dto.LoginRequestDto;
import com.rescue.disaster.dto.RegisterRequestDto;
import com.rescue.disaster.service.AuthService;
import com.rescue.disaster.util.JwtUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtils jwtUtils;

    @Value("${jwt.cookie-name}")
    private String cookieName;

    @Value("${jwt.expiration-ms}")
    private int jwtExpirationMs;

    @Autowired
    public AuthController(AuthService authService, JwtUtils jwtUtils) {
        this.authService = authService;
        this.jwtUtils = jwtUtils;
    }

    @GetMapping("/login")
    public String showLoginPage(@RequestParam(value = "error", required = false) String error, Model model) {
        if (!model.containsAttribute("loginDto")) {
            model.addAttribute("loginDto", new LoginRequestDto());
        }
        if ("denied".equals(error)) {
            model.addAttribute("errorMsg", "Bạn không có quyền truy cập trang này. Vui lòng đăng nhập tài khoản có thẩm quyền!");
        }
        return "auth/login";
    }

    @PostMapping("/login")
    public String handleLogin(@ModelAttribute("loginDto") LoginRequestDto dto,
                              HttpServletResponse response,
                              RedirectAttributes redirectAttributes) {
        try {
            String token = authService.login(dto);

            // Lưu JWT vào HttpOnly Cookie
            Cookie cookie = new Cookie(cookieName, token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(jwtExpirationMs / 1000);
            response.addCookie(cookie);

            // Điều hướng chuẩn theo Role của tài khoản vừa đăng nhập
            if ("admin".equalsIgnoreCase(dto.getUsername())) {
                return "redirect:/analytics/dashboard";
            } else if ("manager".equalsIgnoreCase(dto.getUsername())) {
                return "redirect:/manager/dashboard";
            }
            return "redirect:/map";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Sai tài khoản hoặc mật khẩu!");
            return "redirect:/auth/login";
        }
    }

    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        if (!model.containsAttribute("registerDto")) {
            model.addAttribute("registerDto", new RegisterRequestDto());
        }
        return "auth/register";
    }

    @PostMapping("/register")
    public String handleRegister(@ModelAttribute("registerDto") RegisterRequestDto dto,
                                 RedirectAttributes redirectAttributes) {
        try {
            authService.register(dto);
            redirectAttributes.addFlashAttribute("successMsg", "Đăng ký thành công! Hãy đăng nhập để kích hoạt tài khoản.");
            return "redirect:/auth/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/auth/register";
        }
    }

    @GetMapping("/logout")
    public String handleLogout(HttpServletResponse response) {
        Cookie cookie = new Cookie(cookieName, null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return "redirect:/auth/login";
    }
}