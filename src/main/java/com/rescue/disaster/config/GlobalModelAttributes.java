package com.rescue.disaster.config;

import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute
    public void addGlobalAttributes(Authentication authentication, Model model) {
        boolean isAuthenticated = (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName()));
        model.addAttribute("isAuthenticated", isAuthenticated);

        if (isAuthenticated) {
            model.addAttribute("currentUsername", authentication.getName());
            boolean isAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            boolean isManager = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_MANAGER"));
            model.addAttribute("isAdmin", isAdmin);
            model.addAttribute("isManager", isManager);
            model.addAttribute("isManagerOrAdmin", isAdmin || isManager);
        } else {
            model.addAttribute("isAdmin", false);
            model.addAttribute("isManager", false);
            model.addAttribute("isManagerOrAdmin", false);
        }
    }
}