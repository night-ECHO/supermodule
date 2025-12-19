package com.adflex.customerportal.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "file_name", nullable = false)
    private String fileName;    // Tên file (VD: GPKD.pdf)

    @Column(name = "file_path", nullable = false)
    private String filePath;    // Đường dẫn lưu file

    @Column(name = "is_public")
    private Boolean isPublic = false; // Khách có được xem không?

    @Column(name = "uploaded_at")
    private LocalDateTime uploadedAt = LocalDateTime.now();

    // 👇 LIÊN KẾT VỚI MODULE CŨ
    // Chúng ta lưu ID của Lead (thay vì map cả object để tránh phụ thuộc quá chặt)
    @Column(name = "lead_id", nullable = false)
    private UUID leadId;
}