package com.rescue.disaster.repository;

import com.rescue.disaster.entity.SosRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SosRequestRepository extends JpaRepository<SosRequest, Long> {

    List<SosRequest> findByStatusAndIsDeletedFalse(String status);

    // Chấp nhận cả isDeleted = false hoặc NULL để đảm bảo không bị sót dữ liệu
    @Query("SELECT DISTINCT s FROM SosRequest s LEFT JOIN FETCH s.mediaList WHERE (s.isDeleted = false OR s.isDeleted IS NULL) ORDER BY s.createdAt DESC")
    List<SosRequest> findAllWithMedia();

    @Query("SELECT s FROM SosRequest s LEFT JOIN FETCH s.mediaList WHERE s.sosId = :id")
    Optional<SosRequest> findByIdWithMedia(@Param("id") Long id);
}