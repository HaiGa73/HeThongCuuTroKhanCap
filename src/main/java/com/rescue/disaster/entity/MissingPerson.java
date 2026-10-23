package com.rescue.disaster.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "missing_persons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissingPerson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Long reportId;

    @Column(name = "target_name", nullable = false, length = 150)
    private String targetName; // Tên người cần tìm

    @Column(name = "gender", length = 10)
    private String gender; // Nam / Nữ

    @Column(name = "age")
    private Integer age;

    @Column(name = "photo_url", length = 500)
    private String photoUrl; // Link ảnh trên Cloudinary

    @Column(name = "photo_public_id", length = 255)
    private String photoPublicId;

    @Column(name = "last_seen_location", length = 255)
    private String lastSeenLocation; // Vị trí mất liên lạc lần cuối

    @Column(name = "identifying_characteristics", columnDefinition = "NVARCHAR(MAX)")
    private String identifyingCharacteristics; // Đặc điểm nhận dạng / Trang phục

    @Column(name = "contact_person", nullable = false, length = 150)
    private String contactPerson; // Người nhà đăng tin

    @Column(name = "contact_phone", nullable = false, length = 15)
    private String contactPhone; // Số điện thoại liên hệ khi có tin

    @Builder.Default
    @Column(name = "status", length = 30)
    private String status = "SEARCHING"; // SEARCHING (Đang tìm), FOUND (Đã tìm thấy)

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}