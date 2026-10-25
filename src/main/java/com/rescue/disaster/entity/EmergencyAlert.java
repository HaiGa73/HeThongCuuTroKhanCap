package com.rescue.disaster.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "emergency_alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmergencyAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alert_id")
    private Long alertId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "content", columnDefinition = "NVARCHAR(MAX)", nullable = false)
    private String content;

    @Builder.Default
    @Column(name = "severity", length = 30)
    private String severity = "DANGER"; // INFO, WARNING, DANGER, EXTREME

    @Column(name = "region_id")
    private Integer regionId;

    @Column(name = "issued_by")
    private Long issuedBy;

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}