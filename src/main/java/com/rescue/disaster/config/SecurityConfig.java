package com.rescue.disaster.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Kích hoạt @PreAuthorize trên Controller
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 1. Tài nguyên tĩnh và các trang công khai (Guest truy cập tự do)
                .requestMatchers(
                    "/", "/sos", "/sos/**", 
                    "/map", "/map/**", "/api/map/**",
                    "/relatives/search", 
                    "/survival-guide", "/survival-guide/**",
                    "/auth/**", 
                    "/css/**", "/js/**", "/assets/**", "/images/**", "/webjars/**", 
                    "/ws-rescue/**", "/error"
                ).permitAll()

                // 2. Phân quyền ADMIN (Ban Chỉ Đạo Trung Ương)
                .requestMatchers("/admin/**", "/analytics/**", "/alerts/**").hasRole("ADMIN")

                // 3. Phân quyền MANAGER & ADMIN (Chỉ Huy Vùng điều phối SOS)
                .requestMatchers("/manager/**", "/logistics/inventory", "/logistics/api/**", "/logistics/shipment/**").hasAnyRole("MANAGER", "ADMIN")

                // 4. Phân quyền RESCUE_TEAM & ADMIN (Đội Cứu Hộ trực chiến cano thực địa)
                .requestMatchers("/team/**").hasAnyRole("RESCUE_TEAM", "ADMIN")

                // 5. Phân quyền USER (Công dân đã đăng nhập)
                .requestMatchers("/user/**", "/relatives/checkin", "/relatives/missing/report", "/logistics/donate").authenticated()

                // Tất cả các request còn lại phải xác thực
                .anyRequest().authenticated()
            )
            // Xử lý chuyển hướng tự động khi chưa đăng nhập hoặc không đủ quyền
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.sendRedirect("/auth/login");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.sendRedirect("/auth/login?error=denied");
                })
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}