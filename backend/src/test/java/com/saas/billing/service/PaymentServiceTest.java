package com.saas.billing.service;

import com.saas.billing.dto.PaymentDto;
import com.saas.billing.entity.Payment;
import com.saas.billing.entity.Plan;
import com.saas.billing.exception.ResourceNotFoundException;
import com.saas.billing.repository.PaymentRepository;
import com.saas.billing.serviceImpl.PaymentServiceImpl;
import com.saas.billing.entity.Invoice;
import com.saas.billing.entity.WebhookEvent;
import com.saas.billing.repository.PlanRepository;
import com.saas.billing.repository.SubscriptionRepository;
import com.saas.billing.repository.InvoiceItemRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.saas.billing.entity.Organization;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.saas.billing.repository.OrganizationRepository;
import com.saas.billing.repository.InvoiceRepository;
import com.saas.billing.repository.WebhookEventRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {


    @Mock
    private PaymentRepository paymentRepository;
    
    @Mock
private OrganizationRepository organizationRepository;


@Mock
private InvoiceRepository invoiceRepository;


@Mock
private WebhookEventRepository webhookEventRepository;

@Mock
private PlanRepository planRepository;

@Mock
private SubscriptionRepository subscriptionRepository;

@Mock
private InvoiceItemRepository invoiceItemRepository;

    @InjectMocks
    private PaymentServiceImpl paymentService;



    private UUID paymentId;
    private UUID organizationId;
    private Payment payment;
    private Organization organization;


    @BeforeEach
    void setUp(){


        paymentId = UUID.randomUUID();

        organizationId = UUID.randomUUID();



       organization = Organization.builder()
        .id(organizationId)
        .name("Test Organization")
        .slug("test-org")
        .build();


payment = Payment.builder()
        .id(paymentId)
        .organization(organization)
        .amount(BigDecimal.valueOf(100))
        .currency("USD")
        .status("SUCCEEDED")
        .build();

    }




    @Test
    void shouldGetPaymentHistorySuccessfully(){


        when(paymentRepository
                .findByOrganizationIdOrderByCreatedAtDesc(organizationId))
                .thenReturn(List.of(payment));



        List<PaymentDto> result =
                paymentService.getPaymentHistory(organizationId);



        assertNotNull(result);

        assertEquals(1,result.size());


        verify(paymentRepository)
                .findByOrganizationIdOrderByCreatedAtDesc(organizationId);

    }





    @Test
    void shouldReturnEmptyPaymentHistoryWhenNoPaymentsExist(){


        when(paymentRepository
                .findByOrganizationIdOrderByCreatedAtDesc(organizationId))
                .thenReturn(List.of());



        List<PaymentDto> result =
                paymentService.getPaymentHistory(organizationId);



        assertNotNull(result);

        assertTrue(result.isEmpty());


        verify(paymentRepository)
                .findByOrganizationIdOrderByCreatedAtDesc(organizationId);

    }





    @Test
    void shouldThrowExceptionWhenPaymentNotFound(){


        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.empty());



        assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.refundPayment(paymentId)
        );


        verify(paymentRepository)
                .findById(paymentId);

    }    @Test
    void shouldRefundPaymentSuccessfully(){


        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.of(payment));


        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation ->
                        invocation.getArgument(0));



        paymentService.refundPayment(paymentId);



        assertEquals(
                "REFUNDED",
                payment.getStatus()
        );



        verify(paymentRepository)
                .findById(paymentId);


        verify(paymentRepository)
                .save(payment);

    }





    @Test
    void shouldThrowExceptionWhenRefundingMissingPayment(){


        when(paymentRepository.findById(paymentId))
                .thenReturn(Optional.empty());



        assertThrows(
                ResourceNotFoundException.class,
                () -> paymentService.refundPayment(paymentId)
        );



        verify(paymentRepository)
                .findById(paymentId);



        verify(paymentRepository, never())
                .save(any());

    }   
   @Test
void shouldRetryPaymentSuccessfully() {

    Invoice invoice = Invoice.builder()
            .id(UUID.randomUUID())
            .organization(organization)
            .amountDue(BigDecimal.valueOf(100))
            .build();

    invoice.setStatus("OPEN");

    when(invoiceRepository.findById(invoice.getId()))
            .thenReturn(Optional.of(invoice));

    when(invoiceRepository.save(any(Invoice.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    paymentService.retryPayment(invoice.getId());

    assertEquals("PAID", invoice.getStatus());
    assertEquals(BigDecimal.valueOf(100), invoice.getAmountPaid());
    assertNotNull(invoice.getPaidAt());

    verify(invoiceRepository).findById(invoice.getId());
    verify(invoiceRepository).save(invoice);
}



   @Test
void shouldThrowExceptionWhenRetryingMissingPayment() {

    UUID invoiceId = UUID.randomUUID();

    when(invoiceRepository.findById(invoiceId))
            .thenReturn(Optional.empty());

    assertThrows(
            ResourceNotFoundException.class,
            () -> paymentService.retryPayment(invoiceId)
    );

    verify(invoiceRepository).findById(invoiceId);
    verify(paymentRepository, never()).save(any());
}

 @Test
void shouldCreateCheckoutSessionSuccessfully() {

    UUID planId = UUID.randomUUID();

    Plan plan = Plan.builder()
            .id(planId)
            .name("Professional")
            .stripePriceId("price_mock")
            .build();

    when(organizationRepository.findById(organizationId))
            .thenReturn(Optional.of(organization));

    when(planRepository.findById(planId))
            .thenReturn(Optional.of(plan));

    String successUrl = "http://localhost/success";
    String cancelUrl = "http://localhost/cancel";

    String result = paymentService.createCheckoutSession(
            organizationId,
            planId,
            successUrl,
            cancelUrl);

    assertNotNull(result);
    assertTrue(result.contains("mock_session"));

    verify(organizationRepository).findById(organizationId);
    verify(planRepository).findById(planId);
}



@Test
void shouldHandleStripeWebhookSuccessfully() {

    String payload = """
    {
      "id":"evt_123",
      "object":"event",
      "type":"payment.success",
      "data":{
        "object":{}
      }
    }
    """;

    when(webhookEventRepository.existsByStripeEventId(anyString()))
            .thenReturn(false);

    when(webhookEventRepository.save(any(WebhookEvent.class)))
            .thenAnswer(invocation -> invocation.getArgument(0));

    assertDoesNotThrow(() ->
            paymentService.handleStripeWebhook(
                    payload,
                    "dummy-signature"));

    verify(webhookEventRepository)
            .existsByStripeEventId("evt_123");

    verify(webhookEventRepository, times(2))
            .save(any(WebhookEvent.class));
}
}