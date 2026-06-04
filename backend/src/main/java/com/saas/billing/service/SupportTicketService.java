package com.saas.billing.service;

import com.saas.billing.dto.SupportTicketDto;

import java.util.List;
import java.util.UUID;

public interface SupportTicketService {
    SupportTicketDto createTicket(UUID organizationId, UUID userId, SupportTicketDto dto);
    List<SupportTicketDto> getTickets(UUID organizationId);
    List<SupportTicketDto> getAllTickets();
    SupportTicketDto resolveTicket(UUID ticketId);
}
