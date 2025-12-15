package com.adflex.profile.notification;

import lombok.Builder;
import lombok.Data;

import java.util.Map;


@Data
@Builder
public class NotificationEvent {

    private NotificationType type;

    private String leadId;
    private String phone;
    private String name;
    private String email;


    private Map<String, Object> extra;
}
