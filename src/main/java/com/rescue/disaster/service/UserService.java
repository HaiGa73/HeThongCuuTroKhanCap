package com.rescue.disaster.service;

import com.rescue.disaster.dto.ChangePasswordDto;
import com.rescue.disaster.dto.ProfileUpdateDto;
import com.rescue.disaster.entity.User;
import com.rescue.disaster.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    @Autowired
    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       CloudinaryService cloudinaryService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.cloudinaryService = cloudinaryService;
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng: " + username));
    }

    @Transactional
    public void updateProfile(String username, ProfileUpdateDto dto) {
        User user = getUserByUsername(username);

        user.setFullName(dto.getFullName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setEmail(dto.getEmail());

        // Xử lý upload avatar lên Cloudinary nếu có chọn ảnh mới
        if (dto.getAvatarFile() != null && !dto.getAvatarFile().isEmpty()) {
            try {
                Map<String, Object> uploadResult = cloudinaryService.uploadFile(dto.getAvatarFile(), "user_avatars");
                String avatarUrl = (String) uploadResult.get("secure_url");
                user.setAvatarUrl(avatarUrl);
            } catch (Exception e) {
                log.error("Lỗi tải ảnh đại diện lên Cloudinary: {}", e.getMessage());
                throw new RuntimeException("Không thể tải ảnh đại diện lên Cloudinary: " + e.getMessage());
            }
        }

        userRepository.save(user);
    }

    @Transactional
    public void changePassword(String username, ChangePasswordDto dto) {
        User user = getUserByUsername(username);

        // 1. Kiểm tra mật khẩu hiện tại
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác!");
        }

        // 2. Kiểm tra mật khẩu mới và xác nhận mật khẩu
        if (dto.getNewPassword() == null || dto.getNewPassword().length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải có tối thiểu 6 ký tự!");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Xác nhận mật khẩu mới không khớp!");
        }

        // 3. Không cho đặt trùng mật khẩu cũ
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu mới không được trùng với mật khẩu hiện tại!");
        }

        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }
}