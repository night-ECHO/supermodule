package com.adflex.tracking.controller.admin;

import com.adflex.profile.repository.LeadRepository;
import com.adflex.tracking.dto.admin.AdminDocumentDto;
import com.adflex.tracking.dto.admin.AdminUpdateDocumentRequest;
import com.adflex.tracking.entity.Document;
import com.adflex.tracking.repository.DocumentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
public class AdminDocumentController {

    private static final Path DOCUMENT_UPLOAD_DIR = Path.of("uploads", "documents");

    private final DocumentRepository documentRepository;
    private final LeadRepository leadRepository;

    public AdminDocumentController(DocumentRepository documentRepository, LeadRepository leadRepository) {
        this.documentRepository = documentRepository;
        this.leadRepository = leadRepository;
    }

    @PostMapping("/leads/{leadId}/documents")
    public ResponseEntity<AdminDocumentDto> upload(
            @PathVariable("leadId") String leadId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "is_public", required = false, defaultValue = "false") boolean isPublic
    ) throws IOException {
        UUID leadUuid = UUID.fromString(leadId);
        leadRepository.findById(leadUuid).orElseThrow(() -> new RuntimeException("Lead not found"));

        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is required");
        }

        Files.createDirectories(DOCUMENT_UPLOAD_DIR);

        UUID id = UUID.randomUUID();
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String storedName = (ext != null && !ext.isBlank()) ? id + "." + ext : id.toString();
        Path target = DOCUMENT_UPLOAD_DIR.resolve(storedName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        Document document = Document.builder()
                .id(id)
                .leadId(leadUuid)
                .name(StringUtils.hasText(file.getOriginalFilename()) ? file.getOriginalFilename() : storedName)
                .type(type)
                .isPublic(isPublic)
                .storageKey(target.toString())
                .uploadedAt(Instant.now())
                .build();
        documentRepository.save(document);

        return ResponseEntity.ok(toDto(document));
    }

    @GetMapping("/leads/{leadId}/documents")
    public ResponseEntity<List<AdminDocumentDto>> list(@PathVariable("leadId") String leadId) {
        UUID leadUuid = UUID.fromString(leadId);
        List<AdminDocumentDto> docs = documentRepository.findByLeadIdOrderByUploadedAtDesc(leadUuid).stream()
                .map(this::toDto)
                .toList();
        return ResponseEntity.ok(docs);
    }

    @PatchMapping("/documents/{docId}")
    public ResponseEntity<AdminDocumentDto> update(
            @PathVariable("docId") String docId,
            @RequestBody AdminUpdateDocumentRequest request
    ) {
        UUID id = UUID.fromString(docId);
        Document doc = documentRepository.findById(id).orElseThrow(() -> new RuntimeException("Document not found"));
        if (request != null) {
            if (request.getIsPublic() != null) {
                doc.setIsPublic(request.getIsPublic());
            }
            if (StringUtils.hasText(request.getName())) {
                doc.setName(request.getName());
            }
            if (request.getType() != null) {
                doc.setType(request.getType());
            }
        }
        documentRepository.save(doc);
        return ResponseEntity.ok(toDto(doc));
    }

    private AdminDocumentDto toDto(Document d) {
        return AdminDocumentDto.builder()
                .id(d.getId().toString())
                .leadId(d.getLeadId().toString())
                .name(d.getName())
                .type(d.getType())
                .isPublic(Boolean.TRUE.equals(d.getIsPublic()))
                .uploadedAt(d.getUploadedAt())
                .build();
    }
}
