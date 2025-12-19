package com.adflex.tracking.controller.admin;

import com.adflex.profile.repository.LeadRepository;
import com.adflex.tracking.entity.Document;
import com.adflex.tracking.repository.DocumentRepository;
import lombok.Builder;
import lombok.Data;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.UUID;

/**
 * Internal proof upload for Ultra/Admin to complete milestones.
 * A proof is stored as a Document record and can optionally be public to customers.
 */
@RestController
@RequestMapping("/api/admin/proofs")
public class AdminProofController {

    private static final Path PROOF_UPLOAD_DIR = Path.of("uploads", "documents", "proofs");

    private final DocumentRepository documentRepository;
    private final LeadRepository leadRepository;

    public AdminProofController(DocumentRepository documentRepository, LeadRepository leadRepository) {
        this.documentRepository = documentRepository;
        this.leadRepository = leadRepository;
    }

    @PostMapping
    public ResponseEntity<ProofResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("lead_id") String leadId,
            @RequestParam(value = "milestone_code", required = false) String milestoneCode,
            @RequestParam(value = "is_public", required = false, defaultValue = "false") boolean isPublic
    ) throws IOException {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        UUID leadUuid = UUID.fromString(leadId);
        leadRepository.findById(leadUuid).orElseThrow(() -> new RuntimeException("Lead not found"));

        Files.createDirectories(PROOF_UPLOAD_DIR);

        UUID docId = UUID.randomUUID();
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String storedName = (ext != null && !ext.isBlank()) ? docId + "." + ext : docId.toString();
        Path target = PROOF_UPLOAD_DIR.resolve(storedName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        String originalName = StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : storedName;
        Document doc = Document.builder()
                .id(docId)
                .leadId(leadUuid)
                .name(originalName)
                .type("PROOF")
                .milestoneCode(StringUtils.hasText(milestoneCode) ? milestoneCode : null)
                .storageKey(target.toString())
                .isPublic(isPublic)
                .uploadedAt(Instant.now())
                .build();
        documentRepository.save(doc);

        ProofResponse res = ProofResponse.builder()
                .id(docId.toString())
                .fileName(originalName)
                .storedName(storedName)
                .size(file.getSize())
                .uploadedAt(doc.getUploadedAt().toString())
                .isPublic(Boolean.TRUE.equals(doc.getIsPublic()))
                .fileLink("/api/admin/proofs/" + docId)
                .build();
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{docId}")
    public ResponseEntity<Resource> download(@PathVariable("docId") String docId) throws IOException {
        UUID id = UUID.fromString(docId);
        Document document = documentRepository.findById(id).orElseThrow(() -> new RuntimeException("Document not found"));
        Path file = Path.of(document.getStorageKey());
        if (!Files.exists(file)) {
            return ResponseEntity.notFound().build();
        }

        String contentType = Files.probeContentType(file);
        String filename = StringUtils.hasText(document.getName()) ? document.getName() : file.getFileName().toString();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(contentType != null ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(Files.size(file))
                .body(new FileSystemResource(file));
    }

    @Data
    @Builder
    public static class ProofResponse {
        private String id;
        private String fileName;
        private String storedName;
        private String fileLink;
        private Long size;
        private String uploadedAt;
        private Boolean isPublic;
    }
}

