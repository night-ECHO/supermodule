package com.adflex.profile.controller;

import com.adflex.profile.notification.NotificationLog;
import com.adflex.profile.notification.NotificationLogRepository;
import com.adflex.profile.notification.NotificationService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
public class NotificationAdminController {

    private final NotificationLogRepository logRepo;
    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationLog> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return logRepo.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))).getContent();
    }

    @PostMapping("/{id}/retry")
    public String retry(@PathVariable UUID id, @RequestBody @Valid RetryRequest req) {
        NotificationLog log = logRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Notification log not found: " + id));
        notificationService.retry(log);
        return "OK";
    }

    @GetMapping("/{id}")
    public NotificationLog get(@PathVariable UUID id) {
        return logRepo.findById(id).orElseThrow();
    }

    public record RetryRequest(String note) { }
}
