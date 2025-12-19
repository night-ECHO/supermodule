package com.adflex.customerportal.repository;

import com.adflex.customerportal.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface DocumentRepository extends JpaRepository<Document, UUID> {

    // 1. Tìm tất cả tài liệu của 1 khách (Dùng cho Admin xem)
    List<Document> findByLeadId(UUID leadId);

    // 2. Tìm tài liệu CÔNG KHAI của 1 khách (Dùng cho Khách xem trên Portal)
    List<Document> findByLeadIdAndIsPublicTrue(UUID leadId);
}