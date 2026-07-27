package com.saas.billing.service;

import com.saas.billing.dto.InvoiceDto;
import com.saas.billing.entity.Invoice;
import com.saas.billing.entity.Organization;
import com.saas.billing.exception.ResourceNotFoundException;
import com.saas.billing.repository.InvoiceRepository;
import com.saas.billing.repository.SettingsRepository;
import com.saas.billing.serviceImpl.InvoiceServiceImpl;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import com.saas.billing.entity.Settings;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class InvoiceServiceTest {


    @Mock
    private InvoiceRepository invoiceRepository;


    @Mock
    private SettingsRepository settingsRepository;


    @Mock
    private NotificationService notificationService;


    @InjectMocks
    private InvoiceServiceImpl invoiceService;


    private UUID invoiceId;
    private UUID organizationId;
    private Organization organization;
    private Invoice invoice;



    @BeforeEach
    void setUp(){

        invoiceId = UUID.randomUUID();
        organizationId = UUID.randomUUID();


        organization = Organization.builder()
                .id(organizationId)
                .name("Test Organization")
                .slug("test-org")
                .build();


       invoice = Invoice.builder()
        .id(invoiceId)
        .invoiceNumber("INV-1001")
        .organization(organization)
        .subtotal(BigDecimal.valueOf(1000))
        .discountAmount(BigDecimal.ZERO)
        .total(BigDecimal.valueOf(1000))
        .build();

    }



    @Test
    void shouldGetInvoicesByOrganizationSuccessfully(){


        when(invoiceRepository
                .findByOrganizationIdOrderByCreatedAtDesc(organizationId))
                .thenReturn(List.of(invoice));


        List<InvoiceDto> result =
                invoiceService.getInvoices(organizationId);


        assertNotNull(result);

        assertEquals(1,result.size());


        verify(invoiceRepository)
                .findByOrganizationIdOrderByCreatedAtDesc(organizationId);

    }




    @Test
    void shouldReturnEmptyListWhenNoInvoicesExist(){


        when(invoiceRepository
                .findByOrganizationIdOrderByCreatedAtDesc(organizationId))
                .thenReturn(List.of());


        List<InvoiceDto> result =
                invoiceService.getInvoices(organizationId);


        assertNotNull(result);

        assertTrue(result.isEmpty());


        verify(invoiceRepository)
                .findByOrganizationIdOrderByCreatedAtDesc(organizationId);

    }





    @Test
    void shouldGetInvoiceByIdSuccessfully(){


        when(invoiceRepository.findById(invoiceId))
                .thenReturn(Optional.of(invoice));


        InvoiceDto result =
                invoiceService.getInvoiceById(invoiceId);


        assertNotNull(result);


        verify(invoiceRepository)
                .findById(invoiceId);

    }





    @Test
    void shouldThrowExceptionWhenInvoiceNotFound(){


        when(invoiceRepository.findById(invoiceId))
                .thenReturn(Optional.empty());


        assertThrows(
                ResourceNotFoundException.class,
                () -> invoiceService.getInvoiceById(invoiceId)
        );


        verify(invoiceRepository)
                .findById(invoiceId);

    }

    @Test
    void shouldSendInvoiceEmailSuccessfully(){


        Settings settings = Settings.builder()
                .billingEmail("customer@test.com")
                .build();



        when(invoiceRepository.findById(invoiceId))
                .thenReturn(Optional.of(invoice));


        when(settingsRepository.findByOrganizationId(organizationId))
                .thenReturn(Optional.of(settings));



        invoiceService.emailInvoice(invoiceId);



        verify(invoiceRepository)
                .findById(invoiceId);


        verify(settingsRepository)
                .findByOrganizationId(organizationId);



        verify(notificationService)
                .sendEmail(
                        eq("customer@test.com"),
                        anyString(),
                        anyString()
                );

    }





    @Test
    void shouldSendEmailUsingDefaultOrganizationEmailWhenSettingsMissing(){


        when(invoiceRepository.findById(invoiceId))
                .thenReturn(Optional.of(invoice));


        when(settingsRepository.findByOrganizationId(organizationId))
                .thenReturn(Optional.empty());



        invoiceService.emailInvoice(invoiceId);



        verify(notificationService)
                .sendEmail(
                        eq("test-org@company.com"),
                        anyString(),
                        anyString()
                );

    }





    @Test
    void shouldThrowExceptionWhenEmailInvoiceNotFound(){


        when(invoiceRepository.findById(invoiceId))
                .thenReturn(Optional.empty());



        assertThrows(
                ResourceNotFoundException.class,
                () -> invoiceService.emailInvoice(invoiceId)
        );



        verify(notificationService, never())
                .sendEmail(
                        anyString(),
                        anyString(),
                        anyString()
                );

    }    @Test
    void shouldGeneratePdfInvoiceSuccessfully(){

        invoice.setCreatedAt(OffsetDateTime.now());
invoice.setDueDate(OffsetDateTime.now().plusDays(7));

        when(invoiceRepository.findById(invoiceId))
                .thenReturn(Optional.of(invoice));


        when(settingsRepository.findByOrganizationId(organizationId))
                .thenReturn(Optional.empty());


        when(invoiceRepository.save(any(Invoice.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));



        var result =
                invoiceService.generatePdfInvoice(invoiceId);



        assertNotNull(result);



        verify(invoiceRepository)
                .findById(invoiceId);


        verify(invoiceRepository)
                .save(any(Invoice.class));

    }





    @Test
    void shouldGeneratePdfInvoiceWithSettings(){

        invoice.setCreatedAt(OffsetDateTime.now());
invoice.setDueDate(OffsetDateTime.now().plusDays(7));

        com.saas.billing.entity.Settings settings =
                com.saas.billing.entity.Settings.builder()
                        .country("US")
                        .build();



        when(invoiceRepository.findById(invoiceId))
                .thenReturn(Optional.of(invoice));


        when(settingsRepository.findByOrganizationId(organizationId))
                .thenReturn(Optional.of(settings));


        when(invoiceRepository.save(any(Invoice.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));



        var result =
                invoiceService.generatePdfInvoice(invoiceId);



        assertNotNull(result);



        verify(settingsRepository)
                .findByOrganizationId(organizationId);


        verify(invoiceRepository)
                .save(any(Invoice.class));

    }





    @Test
    void shouldThrowExceptionWhenGeneratingPdfForMissingInvoice(){


        when(invoiceRepository.findById(invoiceId))
                .thenReturn(Optional.empty());



        assertThrows(
                ResourceNotFoundException.class,
                () -> invoiceService.generatePdfInvoice(invoiceId)
        );



        verify(invoiceRepository)
                .findById(invoiceId);


        verify(invoiceRepository, never())
                .save(any());

    }    @Test
    void shouldPropagateExceptionWhenGettingInvoicesFails(){


        when(invoiceRepository
                .findByOrganizationIdOrderByCreatedAtDesc(organizationId))
                .thenThrow(new RuntimeException("Database error"));



        assertThrows(
                RuntimeException.class,
                () -> invoiceService.getInvoices(organizationId)
        );



        verify(invoiceRepository)
                .findByOrganizationIdOrderByCreatedAtDesc(organizationId);

    }
}