package com.rescue.disaster.service;

import com.rescue.disaster.dto.LoginRequestDto;
import com.rescue.disaster.dto.RegisterRequestDto;
import com.rescue.disaster.entity.Role;
import com.rescue.disaster.entity.User;
import com.rescue.disaster.repository.RoleRepository;
import com.rescue.disaster.repository.UserRepository;
import com.rescue.disaster.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

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

        // Gửi mã OTP
        otpService.sendOtp(dto.getEmail(), dto.getUsername(), otp);
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

        otpService.sendOtp(user.getEmail(), user.getUsername(), newOtp);
    }
}