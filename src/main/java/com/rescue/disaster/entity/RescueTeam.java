package com.rescue.disaster.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rescue_teams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RescueTeam {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "team_id")
    private Long teamId;

    @Column(name = "team_name", nullable = false, length = 150)
    private String teamName;

    @Column(name = "leader_name", nullable = false, length = 150)
    private String leaderName;

    @Column(name = "contact_phone", nullable = false, length = 15)
    private String contactPhone;

    @Column(name = "vehicle_type", length = 100)
    private String vehicleType; // Cano máy, Xuồng hơi cứu sinh, Xe lội nước

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;
    
    @Column(name = "vehicle_capacity")
    private Integer vehicleCapacity = 4;

    @Column(name = "status", length = 30)
    private String status = "READY"; // READY, ON_MISSION, MAINTENANCE, OFFLINE

    @Column(name = "region_id", nullable = false)
    private Integer regionId = 1;
}