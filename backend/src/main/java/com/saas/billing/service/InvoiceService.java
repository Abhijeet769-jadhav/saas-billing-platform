package com.saas.billing.service;

import com.saas.billing.dto.InvoiceDto;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;

public interface InvoiceService {
    List<InvoiceDto> getInvoices(UUID organizationId);
    InvoiceDto getInvoiceById(UUID invoiceId);
    ByteArrayInputStream generatePdfInvoice(UUID invoiceId);
    void emailInvoice(UUID invoiceId);
}
