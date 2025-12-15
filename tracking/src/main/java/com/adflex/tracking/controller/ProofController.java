package com.adflex.tracking.controller;

import lombok.Builder;
import lombok.Data;
import org.springframework.core.io.ByteArrayResource;
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
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/admin/proofs")
public class ProofController {

    private static final Path UPLOAD_DIR = Path.of("uploads");

    @PostMapping
    public ResponseEntity<ProofResponse> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        Files.createDirectories(UPLOAD_DIR);

        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String id = UUID.randomUUID().toString();
        String storedName = ext != null && !ext.isBlank() ? id + "." + ext : id;

        Path target = UPLOAD_DIR.resolve(storedName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        ProofResponse res = ProofResponse.builder()
                .id(id)
                .fileName(file.getOriginalFilename())
                .storedName(storedName)
                .size(file.getSize())
                .uploadedAt(Instant.now().toString())
                .fileLink("/api/admin/proofs/" + id)
                .build();

        return ResponseEntity.ok(res);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resource> download(@PathVariable("id") String id) throws IOException {
        if (!StringUtils.hasText(id)) {
            return ResponseEntity.badRequest().build();
        }

        if (!Files.exists(UPLOAD_DIR)) {
            return ResponseEntity.notFound().build();
        }

        Optional<Path> fileOpt;
        try (Stream<Path> stream = Files.list(UPLOAD_DIR)) {
            fileOpt = stream
                    .filter(p -> {
                        String name = p.getFileName().toString();
                        return name.equals(id) || name.startsWith(id + ".");
                    })
                    .findFirst();
        }

        if (fileOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Path file = fileOpt.get();
        String contentType = Files.probeContentType(file);
        ByteArrayResource resource = new ByteArrayResource(Files.readAllBytes(file));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + file.getFileName() + "\"")
                .contentType(contentType != null ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(Files.size(file))
                .body(resource);
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
    }
}
