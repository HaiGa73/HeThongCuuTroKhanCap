package com.rescue.disaster.repository;

import com.rescue.disaster.entity.SafeCheckin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SafeCheckinRepository extends JpaRepository<SafeCheckin, Long> {

    @Query("SELECT s FROM SafeCheckin s LEFT JOIN FETCH s.currentShelter ORDER BY s.checkinTime DESC")
    List<SafeCheckin> findAllWithShelter();

    @Query("SELECT s FROM SafeCheckin s LEFT JOIN FETCH s.currentShelter " +
           "WHERE LOWER(s.fullName) LIKE LOWER(CONCAT('%', :kw, '%')) " +
           "   OR s.phoneNumber LIKE CONCAT('%', :kw, '%') " +
           "   OR s.idCardNumber LIKE CONCAT('%', :kw, '%') " +
           "   OR LOWER(s.originAddress) LIKE LOWER(CONCAT('%', :kw, '%')) " +
           "ORDER BY s.checkinTime DESC")
    List<SafeCheckin> searchSafePeople(@Param("kw") String keyword);
}