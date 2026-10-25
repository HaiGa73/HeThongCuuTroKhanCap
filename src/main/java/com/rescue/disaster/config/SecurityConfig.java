package com.rescue.disaster.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    /*
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Tắt để tiện test API; trong thực tế bật cho POST form Thymeleaf
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Công khai hoàn toàn cho người gặp nạn (Guest)
                .requestMatchers(
                    "/", "/sos", "/sos/**", "/map/**", "/checkin/**", "/missing/**",
                    "/auth/**", "/css/**", "/js/**", "/images/**", "/ws-rescue/**"
                ).permitAll()
                // Phân quyền cho Manager
                .requestMatchers("/manager/**").hasAnyRole("MANAGER", "ADMIN")
                // Phân quyền cho Admin
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Các request còn lại phải đăng nhập
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }*/
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/sos", "/sos/**", "/map", "/map/**", "/checkin/**", "/missing/**",
                    "/alerts/**", // Mở quyền phát và xem cảnh báo
                    "/survival-guide", "/survival-guide/**", // Mở quyền xem cẩm nang
                    "/relatives/**", // <-- MỞ QUYỀN TOÀN BỘ PHÂN HỆ TRA CỨU, BÁO AN, TÌM KIẾM
                    "/logistics/**", // Mở quyền truy cập phân hệ điều phối kho & cứu trợ
                    "/manager/**",
                    "/api/**", // <-- BỔ SUNG DÒNG NÀY ĐỂ MỞ QUYỀN LẤY TỌA ĐỘ
                    "/error", 
                    "/auth/**", "/css/**", "/js/**", "/images/**", "/ws-rescue/**"
                ).permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
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