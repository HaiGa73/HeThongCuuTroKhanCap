package com.rescue.disaster.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "safe_checkins")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SafeCheckin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "checkin_id")
    private Long checkinId;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    @Column(name = "id_card_number", length = 20)
    private String idCardNumber; // CCCD/CMND

    @Column(name = "origin_address", length = 255)
    private String originAddress; // Quê quán / Địa chỉ thường trú

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_shelter_id")
    private Shelter currentShelter; // Trạm sơ tán hiện tại (nếu có)

    @Column(name = "temporary_address", length = 255)
    private String temporaryAddress; // Nơi ở tạm trú bên ngoài

    @Builder.Default
    @Column(name = "health_status", length = 100)
    private String healthStatus = "Bình thường / Đã an toàn";

    @Builder.Default
    @Column(name = "checkin_time")
    private LocalDateTime checkinTime = LocalDateTime.now();
}