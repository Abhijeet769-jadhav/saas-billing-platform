package com.saas.billing.mapper;

import com.saas.billing.entity.*;
import com.saas.billing.dto.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DtoMapper {

    public static UserDto toUserDto(User user) {
        if (user == null) return null;
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .emailVerified(user.getEmailVerified())
                .isActive(user.getIsActive())
                .build();
    }

    public static OrganizationDto toOrganizationDto(Organization organization) {
        if (organization == null) return null;
        return OrganizationDto.builder()
                .id(organization.getId())
                .name(organization.getName())
                .slug(organization.getSlug())
                .build();
    }

    public static PlanFeatureDto toPlanFeatureDto(PlanFeature feature) {
        if (feature == null) return null;
        return PlanFeatureDto.builder()
                .featureKey(feature.getFeatureKey())
                .featureValue(feature.getFeatureValue())
                .build();
    }

    public static PlanDto toPlanDto(Plan plan) {
        if (plan == null) return null;
        List<PlanFeatureDto> features = plan.getFeatures() == null ? Collections.emptyList() :
                plan.getFeatures().stream()
                        .map(DtoMapper::toPlanFeatureDto)
                        .collect(Collectors.toList());

        return PlanDto.builder()
                .id(plan.getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .amount(plan.getAmount())
                .currency(plan.getCurrency())
                .billingInterval(plan.getBillingInterval())
                .trialPeriodDays(plan.getTrialPeriodDays())
                .isActive(plan.getIsActive())
                .features(features)
                .build();
    }

    public static SubscriptionDto toSubscriptionDto(Subscription subscription) {
        if (subscription == null) return null;
        return SubscriptionDto.builder()
                .id(subscription.getId())
                .organizationId(subscription.getOrganization().getId())
                .planId(subscription.getPlan().getId())
                .planName(subscription.getPlan().getName())
                .status(subscription.getStatus())
                .stripeSubscriptionId(subscription.getStripeSubscriptionId())
                .currentPeriodStart(subscription.getCurrentPeriodStart())
                .currentPeriodEnd(subscription.getCurrentPeriodEnd())
                .trialStart(subscription.getTrialStart())
                .trialEnd(subscription.getTrialEnd())
                .cancelAtPeriodEnd(subscription.getCancelAtPeriodEnd())
                .canceledAt(subscription.getCanceledAt())
                .endedAt(subscription.getEndedAt())
                .build();
    }

    public static InvoiceItemDto toInvoiceItemDto(InvoiceItem item) {
        if (item == null) return null;
        return InvoiceItemDto.builder()
                .description(item.getDescription())
                .quantity(item.getQuantity())
                .unitAmount(item.getUnitAmount())
                .amount(item.getAmount())
                .build();
    }

    public static InvoiceDto toInvoiceDto(Invoice invoice) {
        if (invoice == null) return null;
        List<InvoiceItemDto> items = invoice.getItems() == null ? Collections.emptyList() :
                invoice.getItems().stream()
                        .map(DtoMapper::toInvoiceItemDto)
                        .collect(Collectors.toList());

        return InvoiceDto.builder()
                .id(invoice.getId())
                .organizationId(invoice.getOrganization().getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .stripeInvoiceId(invoice.getStripeInvoiceId())
                .amountDue(invoice.getAmountDue())
                .amountPaid(invoice.getAmountPaid())
                .currency(invoice.getCurrency())
                .discountAmount(invoice.getDiscountAmount())
                .taxAmount(invoice.getTaxAmount())
                .cgst(invoice.getCgst())
                .sgst(invoice.getSgst())
                .igst(invoice.getIgst())
                .subtotal(invoice.getSubtotal())
                .total(invoice.getTotal())
                .status(invoice.getStatus())
                .billingReason(invoice.getBillingReason())
                .pdfUrl(invoice.getPdfUrl())
                .dueDate(invoice.getDueDate())
                .paidAt(invoice.getPaidAt())
                .items(items)
                .createdAt(invoice.getCreatedAt())
                .build();
    }

    public static PaymentDto toPaymentDto(Payment payment) {
        if (payment == null) return null;
        return PaymentDto.builder()
                .id(payment.getId())
                .organizationId(payment.getOrganization().getId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .failureMessage(payment.getFailureMessage())
                .createdAt(payment.getCreatedAt())
                .build();
    }

    public static SupportTicketDto toSupportTicketDto(SupportTicket ticket) {
        if (ticket == null) return null;
        return SupportTicketDto.builder()
                .id(ticket.getId())
                .organizationId(ticket.getOrganization().getId())
                .userId(ticket.getUser().getId())
                .subject(ticket.getSubject())
                .description(ticket.getDescription())
                .status(ticket.getStatus())
                .priority(ticket.getPriority())
                .createdAt(ticket.getCreatedAt())
                .build();
    }

    public static NotificationDto toNotificationDto(Notification notification) {
        if (notification == null) return null;
        return NotificationDto.builder()
                .id(notification.getId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
