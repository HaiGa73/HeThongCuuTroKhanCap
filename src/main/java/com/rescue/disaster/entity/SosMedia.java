package com.rescue.disaster.entity;

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
    private SosRequest sosRequest;

    @Column(name = "media_url", nullable = false, length = 500)
    private String mediaUrl;

    @Column(name = "public_id", nullable = false, length = 255)
    private String publicId;

    @Column(name = "media_type", length = 20)
    private String mediaType; // IMAGE, AUDIO

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}