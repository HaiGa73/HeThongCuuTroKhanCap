package com.rescue.disaster.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "supplies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "supply_id")
    private Integer supplyId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "category", nullable = false, length = 50)
    private String category; // FOOD, WATER, MEDICAL, GEAR

    @Column(name = "unit", nullable = false, length = 30)
    private String unit;
}