package com.saas.billing.service;

import com.saas.billing.dto.PaymentDto;

import java.util.List;
import java.util.UUID;

public interface PaymentService {
    String createCheckoutSession(UUID organizationId, UUID planId, String successUrl, String cancelUrl);
    void handleStripeWebhook(String payload, String sigHeader);
    void refundPayment(UUID paymentId);
    void retryPayment(UUID invoiceId);
    List<PaymentDto> getPaymentHistory(UUID organizationId);
}
