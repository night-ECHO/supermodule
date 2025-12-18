package com.adflex.tracking.repository;

import com.adflex.tracking.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository extends JpaRepository<Document, UUID> {
    List<Document> findByLeadIdAndIsPublicTrueOrderByUploadedAtDesc(UUID leadId);

    List<Document> findByLeadIdOrderByUploadedAtDesc(UUID leadId);

    Optional<Document> findByIdAndLeadId(UUID id, UUID leadId);
}
