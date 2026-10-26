package com.rescue.disaster.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "warehouses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "warehouse_id")
    private Long warehouseId;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "location", nullable = false, length = 255)
    private String location;

    @Column(name = "manager_phone", length = 15)
    private String managerPhone;

    @Builder.Default
    @Column(name = "region_id", nullable = false)
    private Integer regionId = 1;
}