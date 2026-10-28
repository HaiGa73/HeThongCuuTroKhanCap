package com.rescue.disaster.service;

import com.rescue.disaster.dto.ChangePasswordDto;
import com.rescue.disaster.dto.ProfileUpdateDto;
import com.rescue.disaster.entity.Role;
import com.rescue.disaster.entity.User;
import com.rescue.disaster.repository.RoleRepository;
import com.rescue.disaster.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;

    @Autowired
    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       PasswordEncoder passwordEncoder,
                       CloudinaryService cloudinaryService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
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

        // Chỉ cập nhật Họ tên và Địa chỉ nhà theo đúng nghiệp vụ mới
        if (dto.getFullName() != null) {
            user.setFullName(dto.getFullName().trim());
        }
        if (dto.getAddress() != null) {
            user.setAddress(dto.getAddress().trim());
        }

        userRepository.save(user);
    }

    @Transactional
    public void changePassword(String username, ChangePasswordDto dto) {
        User user = getUserByUsername(username);

        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu hiện tại không chính xác!");
        }

        if (dto.getNewPassword() == null || dto.getNewPassword().length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải có tối thiểu 6 ký tự!");
        }

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Xác nhận mật khẩu mới không khớp!");
        }

        if (passwordEncoder.matches(dto.getNewPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Mật khẩu mới không được trùng với mật khẩu hiện tại!");
        }

        user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    // 1. Lấy danh sách hoặc tìm kiếm người dùng
    public List<User> searchUsers(String keyword, String role) {
        List<User> list = userRepository.findAll();

        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.trim().toLowerCase();
            list = list.stream()
                    .filter(u -> (u.getUsername() != null && u.getUsername().toLowerCase().contains(kw))
                            || (u.getEmail() != null && u.getEmail().toLowerCase().contains(kw))
                            || (u.getFullName() != null && u.getFullName().toLowerCase().contains(kw))
                            || (u.getPhoneNumber() != null && u.getPhoneNumber().contains(kw)))
                    .toList();
        }

        if (role != null && !role.isBlank()) {
            String targetRole = role.trim();
            list = list.stream()
                    .filter(u -> u.getRoles() != null && u.getRoles().stream()
                            .anyMatch(r -> r.getName() != null && (
                                    r.getName().equalsIgnoreCase(targetRole)
                                    || r.getName().equalsIgnoreCase("ROLE_" + targetRole)
                                    || ("ROLE_" + r.getName()).equalsIgnoreCase(targetRole)
                            )))
                    .toList();
        }

        return list;
    }

    // 2. Cập nhật vai trò (Role)
    @Transactional
    public void updateUserRole(Long userId, String newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng ID: " + userId));

        Role role = roleRepository.findByName(newRole)
                .or(() -> roleRepository.findByName(newRole.startsWith("ROLE_") ? newRole.substring(5) : "ROLE_" + newRole))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vai trò: " + newRole));

        user.getRoles().clear();
        user.getRoles().add(role);
        userRepository.save(user);
    }

    // 3. Khóa / Mở khóa tài khoản (sử dụng trực tiếp trường isActive)
    @Transactional
    public void toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng ID: " + userId));

        boolean currentActive = (user.getIsActive() != null) ? user.getIsActive() : false;
        user.setIsActive(!currentActive);
        userRepository.save(user);
    }
}