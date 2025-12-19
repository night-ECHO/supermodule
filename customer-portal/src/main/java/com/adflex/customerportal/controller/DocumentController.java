package com.adflex.customerportal.controller;

import com.adflex.customerportal.entity.Document;
import com.adflex.customerportal.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentService docService;

    // ========================================================================
    // 1. API UPLOAD (Dành cho Admin/Sale up file lên)
    // ========================================================================
    @PostMapping("/upload")
    public ResponseEntity<?> uploadDocument(
            @RequestParam("leadId") UUID leadId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "isPublic", defaultValue = "false") Boolean isPublic) {

        Document savedDoc = docService.uploadFile(leadId, file, isPublic);
        return ResponseEntity.ok(savedDoc);
    }


    @GetMapping("/list/{leadId}")
    public ResponseEntity<List<Document>> listCustomerDocuments(@PathVariable UUID leadId) {
        // Gọi Service lấy danh sách file (chỉ lấy file public)
        List<Document> docs = docService.getPublicDocumentsForCustomer(leadId);
        return ResponseEntity.ok(docs);
    }


    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable UUID id) {
        // 1. Lấy file từ ổ cứng thông qua Service
        Resource resource = docService.loadFileAsResource(id);


        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}