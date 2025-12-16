package com.adflex.profile.notification;


public interface NotificationService {

    void notify(NotificationEvent event);

    default void retry(NotificationLog log) {
        // optional override
        throw new UnsupportedOperationException("Retry not implemented");
    }
}
