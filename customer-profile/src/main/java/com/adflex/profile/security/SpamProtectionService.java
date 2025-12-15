package com.adflex.profile.security;



import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Slf4j
@Service
public class SpamProtectionService {

    // Cache lưu IP: Key là IP, Value là số lần request
    private final Cache<String, Integer> requestCounts = CacheBuilder.newBuilder()
            .expireAfterWrite(1, TimeUnit.MINUTES) // Tự reset sau 1 phút
            .maximumSize(1000)
            .build();

    // Regex SĐT Việt Nam
    private static final Pattern VN_PHONE_REGEX = Pattern.compile("^(0|84)(3|5|7|8|9)[0-9]{8}$");

    /**
     * Kiểm tra xem IP này có spam không
     */
    public void validateRateLimit(String ip, int limit) {
        Integer attempts = requestCounts.getIfPresent(ip);
        if (attempts == null) {
            attempts = 0;
        }

        if (attempts >= limit) {
            log.warn("⛔ SPAM DETECTED from IP: {}", ip);
            throw new RuntimeException("Bạn thao tác quá nhanh! Vui lòng thử lại sau 1 phút.");
        }

        requestCounts.put(ip, attempts + 1);
    }

    /**
     * Kiểm tra số điện thoại có hợp lệ không
     */
    public void validatePhoneNumber(String phone) {
        if (phone == null || !VN_PHONE_REGEX.matcher(phone).matches()) {
            throw new RuntimeException("Số điện thoại không hợp lệ (Phải là số VN 10 số)");
        }
    }
}