package com.rescue.disaster.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonIgnore;
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    @JsonIgnore
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;
    
    @Column(name = "address")
    private String address;
    
    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = false;
    
    @JsonIgnore
    @Column(name = "otp_code", length = 10)
    private String otpCode;

    @Column(name = "otp_expiry")
    private LocalDateTime otpExpiry;

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
    
    public Boolean getEnabled() {
        return this.isActive != null ? this.isActive : false;
    }

    public void setEnabled(Boolean enabled) {
        this.isActive = enabled;
    }

    // 2. Getter tiện ích lấy tên vai trò đầu tiên để Thymeleaf hiển thị
    public String getRole() {
        if (this.roles == null || this.roles.isEmpty()) {
            return "ROLE_USER";
        }
        Role firstRole = this.roles.iterator().next();
        return (firstRole != null && firstRole.getName() != null) ? firstRole.getName() : "ROLE_USER";
    }

    // 3. Getter tương thích với getId()
    public Long getId() {
        return this.userId;
    }
}