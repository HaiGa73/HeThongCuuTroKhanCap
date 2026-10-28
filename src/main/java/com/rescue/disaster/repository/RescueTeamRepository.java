package com.rescue.disaster.repository;

import com.rescue.disaster.entity.RescueTeam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RescueTeamRepository extends JpaRepository<RescueTeam, Long> {
    List<RescueTeam> findByManager_Username(String username);
    List<RescueTeam> findByManager_UserId(Long userId);
    
 // Tìm đội dựa trên SĐT của tài khoản đăng nhập
    Optional<RescueTeam> findByContactPhone(String contactPhone);
}