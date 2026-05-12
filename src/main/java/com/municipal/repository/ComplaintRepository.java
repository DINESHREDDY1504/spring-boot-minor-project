package com.municipal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.municipal.model.Complaint;

public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    long countByStatus(String status);

    Optional<Complaint> findByComplaintId(String complaintId);

    // 🔥 EXISTING (UNCHANGED)
    List<Complaint> findByIpAddress(String ipAddress);

    List<Complaint> findByImageUrl(String imageUrl);

    // 🔥 NEW: SPAM FEATURES
    long countBySpamTrue();

    List<Complaint> findBySpamTrue();

    List<Complaint> findBySpamFalse();
}
