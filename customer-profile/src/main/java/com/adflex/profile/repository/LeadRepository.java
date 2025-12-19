package com.adflex.profile.repository;

import com.adflex.profile.entity.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LeadRepository extends JpaRepository<Lead, UUID> {

    Optional<Lead> findByPhone(String phone);

    Optional<Lead> findByTrackingToken(String trackingToken);

    // 👇 ĐÂY LÀ HÀM BẠN ĐANG THIẾU (Thêm dòng này vào là hết lỗi)
    Optional<Lead> findByTrackingTokenAndAccessCode(String trackingToken, String accessCode);

    List<Lead> findByFullNameContainingIgnoreCaseOrPhoneContainingIgnoreCaseOrderByCreatedAtDesc(
            String fullName,
            String phone
    );

    List<Lead> findAllByOrderByCreatedAtDesc();
}