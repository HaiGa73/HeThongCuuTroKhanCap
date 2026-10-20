package com.rescue.disaster.repository;

import com.rescue.disaster.entity.SosRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SosRequestRepository extends JpaRepository<SosRequest, Long> {

    // Tìm các ca cứu trợ chưa bị xóa mềm theo trạng thái (ví dụ: PENDING, ASSIGNED)
    List<SosRequest> findByStatusAndIsDeletedFalse(String status);

    // Lấy toàn bộ danh sách chưa xóa sắp xếp theo thời gian mới nhất
    List<SosRequest> findByIsDeletedFalseOrderByCreatedAtDesc();
}