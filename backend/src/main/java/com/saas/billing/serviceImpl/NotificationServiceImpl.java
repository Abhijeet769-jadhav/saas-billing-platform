package com.saas.billing.serviceImpl;

import com.saas.billing.dto.NotificationDto;
import com.saas.billing.entity.*;
import com.saas.billing.exception.ResourceNotFoundException;
import com.saas.billing.mapper.DtoMapper;
import com.saas.billing.repository.NotificationRepository;
import com.saas.billing.repository.OrganizationRepository;
import com.saas.billing.repository.UserRepository;
import com.saas.billing.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;

    @Override
    public void sendEmail(String to, String subject, String body) {
        log.info("Sending Email to: {} | Subject: {} | Body: {}", to, subject, body);
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            message.setFrom("billing@saas.com");
            mailSender.send(message);
            log.info("Email dispatched successfully");
        } catch (Exception e) {
            log.warn("SMTP mail server is offline. Defaulting to terminal log (Email would have been dispatched): {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void createInAppNotification(UUID organizationId, UUID userId, String title, String message) {
        Organization org = organizationId != null ? organizationRepository.findById(organizationId).orElse(null) : null;
        User user = userId != null ? userRepository.findById(userId).orElse(null) : null;

        Notification notif = Notification.builder()
                .organization(org)
                .user(user)
                .title(title)
                .message(message)
                .isRead(false)
                .build();

        notificationRepository.save(notif);
        log.info("In-App Notification saved for user {}: {}", userId, title);
    }

    @Override
    public List<NotificationDto> getNotifications(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(DtoMapper::toNotificationDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void markAsRead(UUID notificationId) {
        Notification notif = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));
        notif.setIsRead(true);
        notificationRepository.save(notif);
    }
}
