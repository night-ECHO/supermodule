package com.adflex.profile.repository;

import com.adflex.profile.entity.Lead;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LeadRepository extends JpaRepository<Lead, UUID> {
    Optional<Lead> findByPhone(String phone);

    Optional<Lead> findByTrackingToken(UUID trackingToken);

    // Dùng cho trang admin: tìm kiếm theo tên hoặc số điện thoại
    java.util.List<Lead> findByFullNameContainingIgnoreCaseOrPhoneContainingIgnoreCaseOrderByCreatedAtDesc(
            String fullName,
            String phone
    );

    java.util.List<Lead> findAllByOrderByCreatedAtDesc();
}
