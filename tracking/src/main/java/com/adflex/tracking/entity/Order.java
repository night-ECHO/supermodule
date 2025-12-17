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
    private String id;  // Giữ String như cũ để không vỡ data hiện tại

    @Column(nullable = false)
    private String leadId;

    @Column(nullable = false)
    private String packageCode;

    // BỔ SUNG MỚI (từ spec PDF)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "addons", columnDefinition = "jsonb")
    private List<String> addons;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount;  // Thay cho amount Long cũ

    @Column(name = "amount")  // Giữ lại để legacy nếu cần
    private Long amount;  // Có thể deprecate sau

    @Enumerated(EnumType.STRING)
    @Column(name = "contract_status")
    private ContractStatus contractStatus = ContractStatus.PENDING;

    @Column(name = "public_token", columnDefinition = "uuid", unique = true)
    private UUID publicToken;

    @Column(name = "payment_confirmed_at")
    private Instant paymentConfirmedAt;

    @Column(name = "payment_confirmed_by", columnDefinition = "uuid")
    private UUID paymentConfirmedBy;

    // CŨ (giữ nguyên)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

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