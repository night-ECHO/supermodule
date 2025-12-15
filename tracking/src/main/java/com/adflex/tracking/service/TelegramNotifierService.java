package com.adflex.tracking.service; // 1. Sửa package cho đúng thư mục

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramNotifierService {

    private final RestTemplate restTemplate = new RestTemplate();

    // 2. Dùng @Value để lấy giá trị từ application.properties (đã link với .env)
    @Value("${telegram.bot-token}")
    private String botToken;

    @Value("${telegram.chat-id}")
    private String chatId;

    public void sendMessage(String message) {
        // Kiểm tra null
        if (botToken == null || chatId == null || botToken.isBlank() || chatId.isBlank()) {
            log.warn("⚠ Missing Telegram config: telegram.bot-token or telegram.chat-id");
            return;
        }

        try {
            String url =
                    "https://api.telegram.org/bot" + botToken + "/sendMessage"
                            + "?chat_id=" + chatId
                            + "&text=" + escape(message)
                            + "&parse_mode=Markdown";

            restTemplate.getForObject(url, String.class);

            log.info("📨 Telegram sent: {}", message);

        } catch (Exception e) {
            log.error("❌ Telegram error: {}", e.getMessage());
        }
    }

    /**
     * Escape ký tự đặc biệt để không lỗi Telegram Markdown
     */
    private String escape(String text) {
        if (text == null) return "";
        return text
                .replace("_", "\\_")
                .replace("*", "\\*")
                .replace("[", "\\[")
                .replace("]", "\\]");
    }
}