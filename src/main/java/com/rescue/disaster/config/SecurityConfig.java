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

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
            	    // 1. Phân quyền ADMIN
            	    .requestMatchers("/alerts/**", "/analytics/**").hasRole("ADMIN")

            	    // 2. Phân quyền MANAGER & ADMIN
            	    .requestMatchers("/manager/**", "/logistics/inventory", "/logistics/api/**").hasAnyRole("MANAGER", "ADMIN")

            	    // 3. Phân quyền USER (Công dân đã đăng nhập)
            	    .requestMatchers("/user/**", "/relatives/checkin", "/relatives/missing/report", "/logistics/donate").authenticated()

            	    // 4. Phân quyền Guest công khai
            	    .requestMatchers(
            	        "/", "/sos", "/sos/**", 
            	        "/map", "/map/**", "/api/map/**",
            	        "/relatives/search", 
            	        "/survival-guide", "/survival-guide/**",
            	        "/auth/**", "/css/**", "/js/**", "/images/**", "/ws-rescue/**", "/error"
            	    ).permitAll()

            	    .anyRequest().authenticated()
            	)
            // Tự động chuyển về trang login nếu chưa đăng nhập hoặc không đủ quyền
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.sendRedirect("/auth/login");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.sendRedirect("/auth/login?error=denied");
                })
            )
            // Tự động chuyển về trang login nếu chưa đăng nhập hoặc không đủ quyền
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