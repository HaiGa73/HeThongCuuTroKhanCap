package com.rescue.disaster.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sos_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SosRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sos_id")
    private Long sosId;

    @Column(name = "sender_name")
    private String senderName;

    @Column(name = "phone_number", nullable = false, length = 15)
    private String phoneNumber;

    @Column(name = "address_detail")
    private String addressDetail;

    @Column(name = "latitude", precision = 10, scale = 7, nullable = false)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7, nullable = false)
    private BigDecimal longitude;

    @Builder.Default
    @Column(name = "number_of_people")
    private Integer numberOfPeople = 1;

    @Builder.Default
    @Column(name = "is_urgent")
    private Boolean isUrgent = true;

    @Builder.Default
    @Column(name = "priority", length = 20)
    private String priority = "HIGH";

    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Builder.Default
    @Column(name = "status", length = 30)
    private String status = "PENDING";

    @Builder.Default
    @Column(name = "is_deleted")
    private Boolean isDeleted = false; // Bắt buộc có @Builder.Default để không bị gán NULL

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @OneToMany(mappedBy = "sosRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SosMedia> mediaList = new ArrayList<>();
}