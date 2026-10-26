package com.rescue.disaster.repository;

import com.rescue.disaster.entity.ReliefDonation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReliefDonationRepository extends JpaRepository<ReliefDonation, Long> {
    @Query("SELECT d FROM ReliefDonation d LEFT JOIN FETCH d.destinationShelter ORDER BY d.createdAt DESC")
    List<ReliefDonation> findAllWithShelter();
}