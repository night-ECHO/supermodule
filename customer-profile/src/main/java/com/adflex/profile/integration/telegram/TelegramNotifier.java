package com.adflex.profile.integration.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
public class TelegramNotifier {

    @Value("${telegram.bot-token}")
    private String botToken;

    @Value("${telegram.chat-id}")
    private String chatId;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Gửi message bất kỳ lên Telegram.
     * Toàn bộ format nội dung (Lead mới, trùng, SLA, Payment...) sẽ được xử lý
     * ở layer Notification (TelegramNotificationService), chỗ này chỉ lo gửi HTTP.
     */
    public void sendMessage(String text) {
        try {
            if (text == null || text.isBlank()) {
                log.warn("Empty telegram message, skip send.");
                return;
            }

            // Log để kiểm tra cấu hình
            log.info("Telegram config - botToken={}, chatId={}", botToken, chatId);

            if (botToken == null || botToken.isBlank()
                    || chatId == null || chatId.isBlank()) {
                log.warn("Telegram botToken/chatId not configured. Skip send.");
                return;
            }

            String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";

            String fullUrl = UriComponentsBuilder.fromHttpUrl(url)
                    .queryParam("chat_id", chatId)
                    .queryParam("text", text)
                    .build()
                    .toUriString();

            log.info("Calling Telegram URL: {}", fullUrl);

            String resp = restTemplate.getForObject(fullUrl, String.class);
            log.info("Telegram response: {}", resp);
        } catch (Exception e) {
            log.error("Failed to send Telegram message", e);
        }
    }
}
