package com.rescue.disaster.config;

import java.io.IOException;
import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                        HttpServletResponse response, 
                                        Authentication authentication) throws IOException, ServletException {
        
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        String redirectUrl = "/"; // Mặc định tài khoản USER chuyển về trang chủ

        for (GrantedAuthority authority : authorities) {
            String role = authority.getAuthority();

            if (role.contains("ADMIN")) {
                redirectUrl = "/analytics/dashboard"; // Dashboard quản trị trung tâm
                break;
            } else if (role.contains("MANAGER")) {
                redirectUrl = "/manager/dashboard";   // Trực ban điều phối cano
                break;
            } else if (role.contains("RESCUE_TEAM") || role.contains("TEAM")) {
                redirectUrl = "/team/mission";        // Trực ban tác chiến thực địa
                break;
            }
        }

        response.sendRedirect(redirectUrl);
    }
}