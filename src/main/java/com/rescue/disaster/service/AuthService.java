package com.rescue.disaster.service;

import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rescue.disaster.dto.LoginRequestDto;
import com.rescue.disaster.dto.RegisterRequestDto;
import com.rescue.disaster.dto.ResetPasswordDto;
import com.rescue.disaster.entity.Role;
import com.rescue.disaster.entity.User;
import com.rescue.disaster.repository.RoleRepository;
import com.rescue.disaster.repository.UserRepository;
import com.rescue.disaster.util.JwtUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final OtpService otpService;

    @Autowired
    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils,
                       OtpService otpService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
        this.otpService = otpService;
    }

    public String login(LoginRequestDto dto) {
        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Sai tài khoản hoặc mật khẩu!"));

        // Kiểm tra tài khoản đã kích hoạt OTP chưa
        if (Boolean.FALSE.equals(user.getIsActive())) {
            throw new IllegalStateException("Tài khoản chưa xác thực mã OTP. Vui lòng hoàn tất kích hoạt!");
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
        );

        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER");

        return jwtUtils.generateToken(dto.getUsername(), role);
    }

    // 1. Đăng ký tài khoản: Lưu tạm và phát mã OTP
    @Transactional
    public void register(RegisterRequestDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại trong hệ thống!");
        }

        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Chưa cấu hình ROLE_USER trong hệ thống"));

        String otp = otpService.generateOtpCode();

        User user = User.builder()
                .username(dto.getUsername())
                .passwordHash(passwordEncoder.encode(dto.getPassword()))
                .fullName(dto.getFullName())
                .phoneNumber(dto.getPhoneNumber())
                .email(dto.getEmail())
                .isActive(false) // Chưa kích hoạt
                .otpCode(otp)
                .otpExpiry(LocalDateTime.now().plusMinutes(5)) // Hạn 5 phút
                .roles(Set.of(userRole))
                .build();

        userRepository.save(user);

        // Gửi mã OTP an toàn với try-catch
        try {
            otpService.sendOtp(dto.getEmail(), dto.getUsername(), otp);
        } catch (Exception e) {
            log.warn(">> Không thể gửi email thực tế qua SMTP (kiểm tra OTP tại Console): {}", e.getMessage());
        }
    }

    // 2. Xác thực mã OTP
    @Transactional
    public void verifyOtp(String username, String enteredOtp) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thông tin tài khoản!"));

        if (Boolean.TRUE.equals(user.getIsActive())) {
            return; // Đã kích hoạt trước đó
        }

        if (user.getOtpCode() == null || !user.getOtpCode().equals(enteredOtp.trim())) {
            throw new IllegalArgumentException("Mã OTP không chính xác!");
        }

        if (user.getOtpExpiry() == null || LocalDateTime.now().isAfter(user.getOtpExpiry())) {
            throw new IllegalArgumentException("Mã OTP đã hết hiệu lực. Vui lòng bấm gửi lại mã mới!");
        }

        // Kích hoạt tài khoản thành công
        user.setIsActive(true);
        user.setOtpCode(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
    }

    // 3. Gửi lại mã OTP mới
    @Transactional
    public void resendOtp(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng!"));

        if (Boolean.TRUE.equals(user.getIsActive())) {
            throw new IllegalArgumentException("Tài khoản này đã được kích hoạt trước đó!");
        }

        String newOtp = otpService.generateOtpCode();
        user.setOtpCode(newOtp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        // Sửa đúng biến lấy từ user và newOtp
        try {
            otpService.sendOtp(user.getEmail(), user.getUsername(), newOtp);
        } catch (Exception e) {
            log.warn(">> Không thể gửi email thực tế qua SMTP (kiểm tra OTP tại Console): {}", e.getMessage());
        }
    }

    // 4. Tạo và gửi mã OTP khôi phục mật khẩu
    public void processForgotPassword(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài khoản với tên đăng nhập này!"));

        otpService.generateAndSendOtp(user.getUsername());
    }

    // 5. Xác thực OTP và đổi mật khẩu mới
    @Transactional
    public void resetPassword(ResetPasswordDto dto) {
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Mật khẩu xác nhận không khớp!");
        }

        boolean isValidOtp = otpService.validateOtp(dto.getUsername(), dto.getOtp());
        if (!isValidOtp) {
            throw new IllegalArgumentException("Mã OTP không hợp lệ hoặc đã hết hạn!");
        }

        User user = userRepository.findByUsername(dto.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại!"));

        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        otpService.clearOtp(dto.getUsername());
    }
}