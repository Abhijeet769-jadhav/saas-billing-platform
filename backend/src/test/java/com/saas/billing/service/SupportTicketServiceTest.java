package com.saas.billing.service;

import com.saas.billing.dto.SupportTicketDto;
import com.saas.billing.entity.Organization;
import com.saas.billing.entity.SupportTicket;
import com.saas.billing.entity.User;
import com.saas.billing.exception.ResourceNotFoundException;
import com.saas.billing.repository.OrganizationRepository;
import com.saas.billing.repository.SupportTicketRepository;
import com.saas.billing.repository.UserRepository;
import com.saas.billing.serviceImpl.SupportTicketServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SupportTicketServiceTest {

    @Mock
    private SupportTicketRepository supportTicketRepository;

    @Mock
    private OrganizationRepository organizationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SupportTicketServiceImpl supportTicketService;

    private UUID organizationId;
    private UUID userId;
    private UUID ticketId;

    private Organization organization;
    private User user;
    private SupportTicket ticket;
    private SupportTicketDto dto;

    @BeforeEach
    void setUp() {

        organizationId = UUID.randomUUID();
        userId = UUID.randomUUID();
        ticketId = UUID.randomUUID();

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

        dto = new SupportTicketDto();
        dto.setSubject("Login Issue");
        dto.setDescription("Unable to login");
        dto.setPriority("HIGH");

        ticket = SupportTicket.builder()
                .id(ticketId)
                .organization(organization)
                .user(user)
                .subject("Login Issue")
                .description("Unable to login")
                .priority("HIGH")
                .status("OPEN")
                .build();
    }

    @Test
    void shouldCreateTicketSuccessfully() {

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.of(organization));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(supportTicketRepository.save(any(SupportTicket.class)))
                .thenReturn(ticket);

        SupportTicketDto result =
                supportTicketService.createTicket(
                        organizationId,
                        userId,
                        dto);

        assertNotNull(result);
        assertEquals("Login Issue", result.getSubject());
        assertEquals("HIGH", result.getPriority());
        assertEquals("OPEN", result.getStatus());

        verify(organizationRepository).findById(organizationId);
        verify(userRepository).findById(userId);
        verify(supportTicketRepository).save(any(SupportTicket.class));
    }

    @Test
    void shouldThrowWhenOrganizationNotFound() {

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> supportTicketService.createTicket(
                        organizationId,
                        userId,
                        dto)
        );

        verify(userRepository, never()).findById(any());
        verify(supportTicketRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenUserNotFound() {

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.of(organization));

        when(userRepository.findById(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> supportTicketService.createTicket(
                        organizationId,
                        userId,
                        dto)
        );

        verify(supportTicketRepository, never()).save(any());
    }    @Test
    void shouldUseDefaultPriorityWhenPriorityIsNull() {

        dto.setPriority(null);

        SupportTicket savedTicket = SupportTicket.builder()
                .id(ticketId)
                .organization(organization)
                .user(user)
                .subject(dto.getSubject())
                .description(dto.getDescription())
                .priority("MEDIUM")
                .status("OPEN")
                .build();

        when(organizationRepository.findById(organizationId))
                .thenReturn(Optional.of(organization));

        when(userRepository.findById(userId))
                .thenReturn(Optional.of(user));

        when(supportTicketRepository.save(any(SupportTicket.class)))
                .thenReturn(savedTicket);

        SupportTicketDto result =
                supportTicketService.createTicket(
                        organizationId,
                        userId,
                        dto);

        assertNotNull(result);
        assertEquals("MEDIUM", result.getPriority());

        verify(supportTicketRepository).save(any(SupportTicket.class));
    }

    @Test
    void shouldGetOrganizationTicketsSuccessfully() {

        when(supportTicketRepository.findByOrganizationIdOrderByCreatedAtDesc(
                organizationId))
                .thenReturn(java.util.List.of(ticket));

        java.util.List<SupportTicketDto> result =
                supportTicketService.getTickets(organizationId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Login Issue", result.get(0).getSubject());
        assertEquals("OPEN", result.get(0).getStatus());

        verify(supportTicketRepository)
                .findByOrganizationIdOrderByCreatedAtDesc(organizationId);
    }

    @Test
    void shouldReturnAllTicketsSuccessfully() {

      when(supportTicketRepository.findAll())
        .thenReturn(java.util.List.of(ticket));

java.util.List<SupportTicketDto> result =
        supportTicketService.getAllTickets();

assertNotNull(result);
assertEquals(1, result.size());
assertEquals("Login Issue", result.get(0).getSubject());

verify(supportTicketRepository).findAll();
    }

    @Test
    void shouldResolveTicketSuccessfully() {

        when(supportTicketRepository.findById(ticketId))
                .thenReturn(Optional.of(ticket));

        when(supportTicketRepository.save(any(SupportTicket.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        SupportTicketDto result =
                supportTicketService.resolveTicket(ticketId);

        assertNotNull(result);
        assertEquals("RESOLVED", result.getStatus());

        verify(supportTicketRepository).findById(ticketId);
        verify(supportTicketRepository).save(ticket);
    }

    @Test
    void shouldThrowWhenResolvingUnknownTicket() {

        when(supportTicketRepository.findById(ticketId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> supportTicketService.resolveTicket(ticketId)
        );

        verify(supportTicketRepository).findById(ticketId);
        verify(supportTicketRepository, never()).save(any());
    }
}