package com.saas.billing.service;

import com.saas.billing.dto.NotificationDto;

import java.util.List;
import java.util.UUID;

public interface NotificationService {
    void sendEmail(String to, String subject, String body);
    void createInAppNotification(UUID organizationId, UUID userId, String title, String message);
    List<NotificationDto> getNotifications(UUID userId);
    void markAsRead(UUID notificationId);
}
