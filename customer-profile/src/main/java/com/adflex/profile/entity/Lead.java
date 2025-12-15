package com.adflex.profile.entity;




import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.type.SqlTypes;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "leads", uniqueConstraints = @UniqueConstraint(columnNames = "phone"))
@Getter
@Setter
@NoArgsConstructor
public class Lead {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "mb_ref_id")
    private String mbRefId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "phone", nullable = false)
    private String phone;

    private String email;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "business_name_options", columnDefinition = "jsonb")
    private List<String> businessNameOptions;


    @Column(name = "business_address")
    private String businessAddress;

    @Column(name = "charter_capital")
    private Long charterCapital;

    @Column(name = "industry_needs", columnDefinition = "text")
    private String industryNeeds;

    @Column(name = "is_duplicate")
    private Boolean isDuplicate = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private LeadStatus status = LeadStatus.NEW;

    @Column(name = "assigned_to_org")
    private String assignedToOrg = "ULTRA";

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}
