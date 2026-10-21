package com.rescue.disaster.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "shelters")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shelter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "shelter_id")
    private Long shelterId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    @Column(name = "latitude", precision = 10, scale = 7, nullable = false)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7, nullable = false)
    private BigDecimal longitude;

    @Builder.Default
    @Column(name = "capacity")
    private Integer capacity = 0;

    @Builder.Default
    @Column(name = "current_occupancy")
    private Integer currentOccupancy = 0;

    @Column(name = "contact_phone", length = 15)
    private String contactPhone;

    @Builder.Default
    @Column(name = "type", length = 50)
    private String type = "SHELTER"; // SHELTER (Trú ẩn), MEDICAL (Y tế), FOOD_DEPOT (Tiếp tế)

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @Builder.Default
    @Column(name = "region_id", nullable = false)
    private Integer regionId = 1;
}