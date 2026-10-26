package com.rescue.disaster.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "sos_media")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SosMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "media_id")
    private Long mediaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sos_id", nullable = false)
    @JsonIgnore 
    private SosRequest sosRequest;

    @Column(name = "media_url", nullable = false, length = 500)
    private String mediaUrl;

    @Column(name = "media_type", length = 30)
    private String mediaType;

    @Column(name = "public_id", length = 255)
    private String publicId;

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}