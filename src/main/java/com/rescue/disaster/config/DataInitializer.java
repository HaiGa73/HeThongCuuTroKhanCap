package com.rescue.disaster.config;

import com.rescue.disaster.entity.Role;
import com.rescue.disaster.entity.User;
import com.rescue.disaster.repository.RoleRepository;
import com.rescue.disaster.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(UserRepository userRepository,
                           RoleRepository roleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // 1. Đảm bảo 3 Role cơ bản luôn tồn tại
        Role adminRole = getOrCreateRole("ROLE_ADMIN", "Quản trị viên Ban chỉ đạo");
        Role managerRole = getOrCreateRole("ROLE_MANAGER", "Đội trưởng cứu nạn khu vực");
        Role userRole = getOrCreateRole("ROLE_USER", "Công dân / Tình nguyện viên");

        // 2. Khởi tạo / Đồng bộ lại mật khẩu chuẩn '123456'
        initUser("admin", "123456", "Ban Chỉ Đạo Trung Ương", "0901000001", "admin@rescue.vn", adminRole);
        initUser("manager", "123456", "Trưởng Đội Cano Cứu Nạn", "0902000002", "manager@rescue.vn", managerRole);
        initUser("user", "123456", "Nguyễn Văn A (Công Dân)", "0903000003", "user@rescue.vn", userRole);

        log.info(">> Đã khởi tạo và mã hóa BCrypt thành công cho admin, manager, user với mật khẩu: 123456");
    }

    private Role getOrCreateRole(String roleName, String description) {
        return roleRepository.findByName(roleName).orElseGet(() -> 
            roleRepository.save(Role.builder().name(roleName).description(description).build())
        );
    }

    private void initUser(String username, String rawPassword, String fullName, String phone, String email, Role role) {
        User user = userRepository.findByUsername(username).orElseGet(() -> 
            User.builder().username(username).build()
        );

        // Dùng chính PasswordEncoder của Spring để sinh chuỗi hash chính xác 100%
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setFullName(fullName);
        user.setPhoneNumber(phone);
        user.setEmail(email);
        user.setIsActive(true);
        user.setRoles(Set.of(role));

        userRepository.save(user);
    }
}