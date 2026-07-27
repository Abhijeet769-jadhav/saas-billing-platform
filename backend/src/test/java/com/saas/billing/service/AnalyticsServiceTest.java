package com.saas.billing.service;

import com.saas.billing.dto.AnalyticsDto;
import com.saas.billing.entity.Invoice;
import com.saas.billing.entity.Organization;
import com.saas.billing.entity.Payment;
import com.saas.billing.entity.Plan;
import com.saas.billing.entity.Subscription;
import com.saas.billing.repository.InvoiceRepository;
import com.saas.billing.repository.PaymentRepository;
import com.saas.billing.repository.PlanRepository;
import com.saas.billing.repository.SubscriptionRepository;
import com.saas.billing.serviceImpl.AnalyticsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;
import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private PlanRepository planRepository;

    @InjectMocks
    private AnalyticsServiceImpl analyticsService;

    private UUID organizationId;
    private Organization organization;
    private Plan plan;
    private Subscription subscription;
    private Invoice invoice;
    private Payment payment;

    @BeforeEach
    void setUp() {

        organizationId = UUID.randomUUID();

        organization = Organization.builder()
                .id(organizationId)
                .name("Test Organization")
                .build();

        plan = Plan.builder()
                .id(UUID.randomUUID())
                .name("Pro")
                .amount(new BigDecimal("100.00"))
                .billingInterval("monthly")
                .build();

        subscription = Subscription.builder()
                .organization(organization)
                .plan(plan)
                .status("ACTIVE")
                .build();

        invoice = Invoice.builder()
                .invoiceNumber("INV-001")
                .organization(organization)
                .subscription(subscription)
                .subtotal(new BigDecimal("100.00"))
                .taxAmount(new BigDecimal("10.00"))
                .discountAmount(BigDecimal.ZERO)
                .total(new BigDecimal("110.00"))
                .status("PAID")
                .createdAt(OffsetDateTime.now())
                .paidAt(OffsetDateTime.now())
                .build();

        payment = Payment.builder()
                .status("SUCCEEDED")
                .amount(new BigDecimal("110.00"))
                .build();
    }

    @Test
    void shouldReturnPlatformAnalyticsSuccessfully() {

        when(subscriptionRepository.findAll())
                .thenReturn(List.of(subscription));

        when(invoiceRepository.findAll())
                .thenReturn(List.of(invoice));

        when(paymentRepository.findAll())
                .thenReturn(List.of(payment));

        when(planRepository.findAll())
                .thenReturn(List.of(plan));

        AnalyticsDto result =
                analyticsService.getPlatformAnalytics();

        assertNotNull(result);
        assertEquals(new BigDecimal("100.00"), result.getMrr());
        assertEquals(new BigDecimal("1200.00"), result.getArr());
        assertEquals(1L, result.getActiveSubscribers());
        assertEquals(100.0, result.getPaymentSuccessRate());

        assertTrue(result.getPlanDistribution().containsKey("Pro"));
        assertTrue(result.getRevenueByPlan().containsKey("Pro"));

        verify(subscriptionRepository).findAll();
        verify(invoiceRepository).findAll();
        verify(paymentRepository).findAll();
        verify(planRepository).findAll();
    }

    @Test
    void shouldReturnEmptyPlatformAnalytics() {

        when(subscriptionRepository.findAll())
                .thenReturn(List.of());

        when(invoiceRepository.findAll())
                .thenReturn(List.of());

        when(paymentRepository.findAll())
                .thenReturn(List.of());

        when(planRepository.findAll())
                .thenReturn(List.of());

        AnalyticsDto result =
                analyticsService.getPlatformAnalytics();

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getMrr());
        assertEquals(BigDecimal.ZERO, result.getArr());
        assertEquals(0L, result.getActiveSubscribers());
        assertEquals(100.0, result.getPaymentSuccessRate());

        verify(subscriptionRepository).findAll();
        verify(invoiceRepository).findAll();
        verify(paymentRepository).findAll();
        verify(planRepository).findAll();
    }    @Test
    void shouldReturnOrganizationAnalyticsSuccessfully() {

        when(subscriptionRepository.findAll())
                .thenReturn(List.of(subscription));

        when(invoiceRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId))
                .thenReturn(List.of(invoice));

        AnalyticsDto result =
                analyticsService.getOrganizationAnalytics(organizationId);

        assertNotNull(result);
        assertEquals(new BigDecimal("100.00"), result.getMrr());
        assertEquals(new BigDecimal("1200.00"), result.getArr());
        assertEquals(1L, result.getActiveSubscribers());
        assertEquals(100.0, result.getPaymentSuccessRate());

        assertNotNull(result.getRevenueByMonth());
        assertEquals(1, result.getRevenueByMonth().size());

        verify(subscriptionRepository).findAll();
        verify(invoiceRepository)
                .findByOrganizationIdOrderByCreatedAtDesc(organizationId);
    }

    @Test
    void shouldReturnOrganizationAnalyticsWithNoInvoices() {

        when(subscriptionRepository.findAll())
                .thenReturn(List.of());

        when(invoiceRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId))
                .thenReturn(List.of());

        AnalyticsDto result =
                analyticsService.getOrganizationAnalytics(organizationId);

        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result.getMrr());
        assertEquals(BigDecimal.ZERO, result.getArr());
        assertEquals(0L, result.getActiveSubscribers());
        assertTrue(result.getRevenueByMonth().isEmpty());

        verify(subscriptionRepository).findAll();
        verify(invoiceRepository)
                .findByOrganizationIdOrderByCreatedAtDesc(organizationId);
    }

    @Test
    void shouldExportRevenueReportCsvSuccessfully() throws Exception {

        when(invoiceRepository.findAll())
                .thenReturn(List.of(invoice));

        ByteArrayInputStream stream =
                analyticsService.exportRevenueReportCsv();

        assertNotNull(stream);

        String csv = new String(stream.readAllBytes());

        assertTrue(csv.contains("Invoice Number"));
        assertTrue(csv.contains("INV-001"));
        assertTrue(csv.contains("Test Organization"));
        assertTrue(csv.contains("Pro"));
        assertTrue(csv.contains("110.00"));

        verify(invoiceRepository).findAll();
    }

    @Test
    void shouldExportEmptyRevenueReportCsvSuccessfully() throws Exception {

        when(invoiceRepository.findAll())
                .thenReturn(List.of());

        ByteArrayInputStream stream =
                analyticsService.exportRevenueReportCsv();

        assertNotNull(stream);

        String csv = new String(stream.readAllBytes());

        assertTrue(csv.contains("Invoice Number"));
        assertFalse(csv.contains("INV-001"));

        verify(invoiceRepository).findAll();
    }
}