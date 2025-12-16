package com.adflex.profile.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ZaloZnsService {

    private final NotificationLogRepository logRepo;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${zalo.oa.access-token:}")
    private String zaloAccessToken;

    public NotificationLog notifyZalo(UUID leadId, String phoneNumber, String eventType, Map<String, Object> params) {
        String templateId = ZaloTemplateMapping.EVENT_TO_TEMPLATE.get(eventType);
        String sanitizedPhone = sanitizePhone(phoneNumber);

        NotificationLog logRow = NotificationLog.builder()
                .leadId(leadId)
                .channel(NotificationChannel.ZALO_ZNS)
                .eventType(eventType)
                .recipient(sanitizedPhone)
                .contentPreview(params == null ? "" : params.toString())
                .status(NotificationStatus.PENDING)
                .build();
        logRow = logRepo.save(logRow);

        try {
            if (templateId == null) {
                throw new IllegalArgumentException("Template mapping not found for event: " + eventType);
            }
            if (zaloAccessToken == null || zaloAccessToken.isBlank()) {
                throw new IllegalStateException("Missing ZALO OA access token");
            }

            Map<String, Object> body = new HashMap<>();
            body.put("phone", sanitizedPhone);
            body.put("template_id", templateId);
            body.put("template_data", params == null ? Map.of() : params);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(zaloAccessToken);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
            restTemplate.postForObject("https://business.openapi.zalo.me/message/template", request, Map.class);

            logRow.setStatus(NotificationStatus.SENT);
            logRow.setSentAt(LocalDateTime.now());
            logRepo.save(logRow);
            return logRow;
        } catch (Exception e) {
            logRow.setStatus(NotificationStatus.FAILED);
            logRow.setErrorMessage(e.getMessage());
            logRepo.save(logRow);

            log.warn("Zalo send failed but business continues: {}", e.getMessage());
            return logRow;
        }
    }

    private String sanitizePhone(String phone) {
        if (phone == null || phone.isBlank()) return "";
        String digits = phone.replaceAll("[^0-9]", "");
        if (digits.startsWith("0")) {
            digits = "84" + digits.substring(1);
        } else if (!digits.startsWith("84")) {
            digits = "84" + digits;
        }
        return digits;
    }
}
