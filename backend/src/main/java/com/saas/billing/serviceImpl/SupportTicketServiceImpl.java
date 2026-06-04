package com.saas.billing.serviceImpl;

import com.saas.billing.dto.SupportTicketDto;
import com.saas.billing.entity.*;
import com.saas.billing.exception.ResourceNotFoundException;
import com.saas.billing.mapper.DtoMapper;
import com.saas.billing.repository.OrganizationRepository;
import com.saas.billing.repository.SupportTicketRepository;
import com.saas.billing.repository.UserRepository;
import com.saas.billing.service.SupportTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupportTicketServiceImpl implements SupportTicketService {

    private final SupportTicketRepository supportTicketRepository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public SupportTicketDto createTicket(UUID organizationId, UUID userId, SupportTicketDto dto) {
        Organization org = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        SupportTicket ticket = SupportTicket.builder()
                .organization(org)
                .user(user)
                .subject(dto.getSubject())
                .description(dto.getDescription())
                .status("OPEN")
                .priority(dto.getPriority() != null ? dto.getPriority() : "MEDIUM")
                .build();

        SupportTicket saved = supportTicketRepository.save(ticket);
        return DtoMapper.toSupportTicketDto(saved);
    }

    @Override
    public List<SupportTicketDto> getTickets(UUID organizationId) {
        return supportTicketRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId).stream()
                .map(DtoMapper::toSupportTicketDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<SupportTicketDto> getAllTickets() {
        return supportTicketRepository.findAll().stream()
                .map(DtoMapper::toSupportTicketDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SupportTicketDto resolveTicket(UUID ticketId) {
        SupportTicket ticket = supportTicketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        ticket.setStatus("RESOLVED");
        SupportTicket saved = supportTicketRepository.save(ticket);
        return DtoMapper.toSupportTicketDto(saved);
    }
}
