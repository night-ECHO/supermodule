package com.adflex.tracking.entity;

import com.adflex.tracking.enums.ContractStatus;
import com.adflex.tracking.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;  // Keep as String to avoid breaking existing data

    @Column(name = "lead_id", nullable = false)
    private String leadId;

    @Column(name = "package_code", nullable = false)
    private String packageCode;

    // New: Addons as JSONB array
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "addons", columnDefinition = "jsonb")
    private List<String> addons;

    // New: Precise total amount calculated server-side
    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount;

    // Legacy field - keep for backward compatibility (can deprecate later)
    @Column(name = "amount")
    private Long amount;

    // Payment status
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    // Contract status
    @Enumerated(EnumType.STRING)
    @Column(name = "contract_status")
    private ContractStatus contractStatus = ContractStatus.PENDING;

    // Public access token for payment link (no login required)
    @Column(name = "public_token", columnDefinition = "uuid", unique = true)
    private UUID publicToken;

    // Manual confirmation by AdFlex Ops
    @Column(name = "payment_confirmed_at")
    private Instant paymentConfirmedAt;

    @Column(name = "payment_confirmed_by", columnDefinition = "uuid")
    private UUID paymentConfirmedBy;

    // Old fields (kept for compatibility)
    private LocalDateTime paidAt;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}