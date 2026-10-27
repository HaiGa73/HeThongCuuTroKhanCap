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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // Tài khoản gửi SOS (NULL nếu là Guest vãng lai)

    @Column(name = "sender_name", nullable = false, length = 150)
    private String senderName;

    @Column(name = "phone_number", nullable = false, length = 15)
    private String phoneNumber;

    @Column(name = "latitude", precision = 10, scale = 7, nullable = false)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7, nullable = false)
    private BigDecimal longitude;

    @Column(name = "address_detail", length = 255)
    private String addressDetail;

    @Builder.Default
    @Column(name = "number_of_people")
    private Integer numberOfPeople = 1;

    @Builder.Default
    @Column(name = "priority", length = 20)
    private String priority = "HIGH";

    @Builder.Default
    @Column(name = "status", length = 30)
    private String status = "PENDING"; // PENDING, ASSIGNED, IN_PROGRESS, COMPLETED, CANCELLED
    
    @Column(name = "manager_note", columnDefinition = "NVARCHAR(MAX)")
    private String managerNote;
    
    @Column(name = "description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Builder.Default
    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "sosRequest", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SosMedia> mediaList = new ArrayList<>();
}