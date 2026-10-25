package com.rescue.disaster.repository;

import com.rescue.disaster.entity.MissingPerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissingPersonRepository extends JpaRepository<MissingPerson, Long> {

    List<MissingPerson> findAllByOrderByCreatedAtDesc();

    @Query("SELECT m FROM MissingPerson m " +
           "WHERE LOWER(m.targetName) LIKE LOWER(CONCAT('%', :kw, '%')) " +
           "   OR m.contactPhone LIKE CONCAT('%', :kw, '%') " +
           "   OR LOWER(m.lastSeenLocation) LIKE LOWER(CONCAT('%', :kw, '%')) " +
           "ORDER BY m.createdAt DESC")
    List<MissingPerson> searchMissingPeople(@Param("kw") String keyword);
}