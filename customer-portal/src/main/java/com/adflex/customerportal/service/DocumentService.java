package com.adflex.customerportal.service;

import com.adflex.customerportal.entity.Document;
import com.adflex.customerportal.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository docRepo;

    // Đường dẫn thư mục lưu file (Tự động tạo folder 'uploads' ở thư mục gốc dự án)
    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

    // Constructor: Tự động tạo thư mục khi khởi động Service
    public DocumentService() {
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Không thể tạo thư mục lưu file!", ex);
        }
    }

    // ========================================================================
    // 1. TÍNH NĂNG UPLOAD FILE (Code cũ)
    // ========================================================================
    public Document uploadFile(UUID leadId, MultipartFile file, Boolean isPublic) {
        try {
            // Chuẩn hóa tên file
            String fileName = file.getOriginalFilename();
            if (fileName == null || fileName.contains("..")) {
                throw new RuntimeException("Tên file không hợp lệ: " + fileName);
            }

            // Lưu file vật lý vào ổ cứng
            Path targetLocation = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Lưu thông tin vào Database
            Document doc = new Document();
            doc.setLeadId(leadId);
            doc.setFileName(fileName);
            doc.setFilePath(targetLocation.toString());
            doc.setIsPublic(isPublic != null ? isPublic : false); // Mặc định là ẩn nếu null

            return docRepo.save(doc);

        } catch (IOException ex) {
            throw new RuntimeException("Lỗi khi lưu file " + file.getOriginalFilename(), ex);
        }
    }

    // ========================================================================
    // 2. TÍNH NĂNG LẤY DANH SÁCH (Phục vụ Scenario 2)
    // ========================================================================
    public List<Document> getPublicDocumentsForCustomer(UUID leadId) {
        // Chỉ trả về các file được đánh dấu Public
        return docRepo.findByLeadIdAndIsPublicTrue(leadId);
    }

    // ========================================================================
    // 3. TÍNH NĂNG TẢI FILE (Phục vụ Scenario 4)
    // ========================================================================
    public Resource loadFileAsResource(UUID documentId) {
        try {
            // Bước 1: Tìm file trong DB
            Document doc = docRepo.findById(documentId)
                    .orElseThrow(() -> new RuntimeException("File không tồn tại với ID: " + documentId));

            // Bước 2: BẢO MẬT - Kiểm tra quyền xem
            // Nếu file đang để ẩn (isPublic = false) thì chặn ngay
            if (!Boolean.TRUE.equals(doc.getIsPublic())) {
                throw new RuntimeException("Bạn không có quyền truy cập tài liệu nội bộ này!");
            }

            // Bước 3: Lấy file từ ổ cứng
            Path filePath = Paths.get(doc.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Không tìm thấy file vật lý trên ổ cứng: " + doc.getFileName());
            }

        } catch (MalformedURLException ex) {
            throw new RuntimeException("Đường dẫn file bị lỗi: " + documentId, ex);
        }
    }
}