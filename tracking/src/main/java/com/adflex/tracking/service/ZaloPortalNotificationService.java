package com.adflex.tracking.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Stub service: later plug in real Zalo OA API call.
 */
@Slf4j
@Service
public class ZaloPortalNotificationService {

    private final String zaloAccessToken;
    private final String portalBaseUrl;

    public ZaloPortalNotificationService(
            @Value("${zalo.oa.access-token:}") String zaloAccessToken,
            @Value("${customer.portal.base-url:https://portal.adflex.vn/track}") String portalBaseUrl
    ) {
        this.zaloAccessToken = zaloAccessToken;
        this.portalBaseUrl = portalBaseUrl;
    }

    public void sendPortalAccess(String phone, String trackingToken, String accessCodePlain) {
        String link = portalBaseUrl.endsWith("/")
                ? portalBaseUrl + trackingToken
                : portalBaseUrl + "/" + trackingToken;

        if (zaloAccessToken == null || zaloAccessToken.isBlank()) {
            log.info("📨 [ZALO STUB] Would send to {}: Link={} Pass={}", phone, link, accessCodePlain);
            return;
        }

        // TODO: integrate Zalo OA API
        log.info("📨 [ZALO TODO] Send to {} with access-token configured. Link={} Pass={}", phone, link, accessCodePlain);
    }
}

