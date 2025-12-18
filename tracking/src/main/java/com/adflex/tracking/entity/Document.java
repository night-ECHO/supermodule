package com.adflex.tracking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "lead_id", nullable = false, columnDefinition = "uuid")
    private UUID leadId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type")
    private String type;

    @Column(name = "milestone_code")
    private String milestoneCode;

    /**
     * Storage key/path for the actual file.
     * For now, local filesystem path under tracking's working dir.
     */
    @Column(name = "storage_key", nullable = false)
    private String storageKey;

    @Column(name = "is_public", nullable = false)
    private Boolean isPublic = Boolean.FALSE;

    @Column(name = "uploaded_at", nullable = false)
    private Instant uploadedAt;

    @PrePersist
    void prePersist() {
        if (uploadedAt == null) {
            uploadedAt = Instant.now();
        }
        if (isPublic == null) {
            isPublic = Boolean.FALSE;
        }
    }
}
