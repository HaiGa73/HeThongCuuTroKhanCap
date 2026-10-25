package com.rescue.disaster.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "relief_donations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReliefDonation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "donation_id")
    private Long donationId;

    @Column(name = "donor_name", nullable = false, length = 150)
    private String donorName;

    @Column(name = "phone_number", nullable = false, length = 15)
    private String phoneNumber;

    @Column(name = "organization", length = 200)
    private String organization;

    @Column(name = "supply_type", nullable = false, length = 100)
    private String supplyType;

    @Column(name = "quantity_detail", nullable = false, length = 255)
    private String quantityDetail;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "destination_shelter_id")
    private Shelter destinationShelter;

    @Column(name = "expected_delivery")
    private LocalDateTime expectedDelivery;

    @Builder.Default
    @Column(name = "status", length = 30)
    private String status = "REGISTERED"; // REGISTERED, EN_ROUTE, RECEIVED

    @Column(name = "note", columnDefinition = "NVARCHAR(MAX)")
    private String note;

    @Builder.Default
    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}