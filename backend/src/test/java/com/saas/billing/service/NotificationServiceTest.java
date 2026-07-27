package com.saas.billing.service;

import com.saas.billing.dto.NotificationDto;
import com.saas.billing.entity.Notification;
import com.saas.billing.entity.Organization;
import com.saas.billing.entity.User;
import com.saas.billing.exception.ResourceNotFoundException;
import com.saas.billing.repository.NotificationRepository;
import com.saas.billing.repository.OrganizationRepository;
import com.saas.billing.repository.UserRepository;
import com.saas.billing.serviceImpl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    private UUID organizationId;
    private UUID userId;
    private UUID notificationId;

    private Organization organization;
    private User user;
    private Notification notification;

    @BeforeEach
    void setUp() {

        organizationId = UUID.randomUUID();
        userId = UUID.randomUUID();
        notificationId = UUID.randomUUID();

        organization = Organization.builder()
                .id(organizationId)
                .name("Test Organization")
                .build();

        user = User.builder()
                .id(userId)
                .email("john@test.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        notification = Notification.builder()
                .id(notificationId)
                .organization(organization)
                .user(user)
                .title("Test Title")
                .message("Test Message")
                .isRead(false)
                .build();
    }

    @Test
    void shouldSendEmailSuccessfully() {

        doNothing().when(mailSender)
                .send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() ->
                notificationService.sendEmail(
                        "john@test.com",
                        "Subject",
                        "Body"));

        ArgumentCaptor<SimpleMailMessage> captor =
                ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();

        assertEquals("Subject", message.getSubject());
        assertEquals("Body", message.getText());
        assertEquals("billing@saas.com", message.getFrom());
        assertArrayEquals(
                new String[]{"john@test.com"},
                message.getTo());
    }

    @Test
    void shouldNotThrowWhenMailSenderFails() {

        doThrow(new RuntimeException("SMTP Down"))
                .when(mailSender)
                .send(any(SimpleMailMessage.class));

        assertDoesNotThrow(() ->
                notificationService.sendEmail(
                        "john@test.com",
                        "Subject",
                        "Body"));

        verify(mailSender)
                .send(any(SimpleMailMessage.class));
    }

    @Test
    void shouldCreateInAppNotificationSuccessfully() {

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.of(organization));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() ->
                notificationService.createInAppNotification(
                        organizationId,
                        userId,
                        "Welcome",
                        "Hello"));

        verify(organizationRepository).findById(organizationId);
        verify(userRepository).findById(userId);
        verify(notificationRepository).save(any(Notification.class));
    }    @Test
    void shouldCreateNotificationWithNullOrganizationAndUser() {

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.empty());

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() ->
                notificationService.createInAppNotification(
                        organizationId,
                        userId,
                        "System",
                        "Background notification"));

        verify(organizationRepository).findById(organizationId);
        verify(userRepository).findById(userId);
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void shouldGetNotificationsSuccessfully() {

        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(notification));

        List<NotificationDto> result =
                notificationService.getNotifications(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Title", result.get(0).getTitle());
        assertEquals("Test Message", result.get(0).getMessage());

        verify(notificationRepository)
                .findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    void shouldReturnEmptyNotificationList() {

        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of());

        List<NotificationDto> result =
                notificationService.getNotifications(userId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(notificationRepository)
                .findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Test
    void shouldMarkNotificationAsReadSuccessfully() {

        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.of(notification));

        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        assertDoesNotThrow(() ->
                notificationService.markAsRead(notificationId));

        assertTrue(notification.getIsRead());

        verify(notificationRepository).findById(notificationId);
        verify(notificationRepository).save(notification);
    }

    @Test
    void shouldThrowWhenNotificationNotFound() {

        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> notificationService.markAsRead(notificationId)
        );

        verify(notificationRepository).findById(notificationId);
        verify(notificationRepository, never()).save(any());
    }
}