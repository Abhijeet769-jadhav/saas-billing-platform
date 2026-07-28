package com.saas.billing.controller;

import com.saas.billing.dto.InvoiceDto;
import com.saas.billing.security.CustomUserDetails;
import com.saas.billing.service.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Invoices", description = "Endpoints for retrieving invoices and downloading billing PDF files")
public class InvoiceController {

    private final InvoiceService invoiceService;

    @GetMapping
    @Operation(summary = "Get list of all invoices for current organization")
    public ResponseEntity<List<InvoiceDto>> getInvoices(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(invoiceService.getInvoices(userDetails.getOrganizationId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get details of specific invoice")
    public ResponseEntity<InvoiceDto> getInvoiceById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(invoiceService.getInvoiceById(id));
    }

    @GetMapping(value = "/{id}/download", produces = MediaType.APPLICATION_PDF_VALUE)
    @Operation(summary = "Download generated invoice PDF file")
    public ResponseEntity<InputStreamResource> downloadInvoice(@PathVariable("id") UUID id) {
        ByteArrayInputStream pdfStream = invoiceService.generatePdfInvoice(id);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "inline; filename=invoice-" + id + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdfStream));
    }

    @PostMapping("/{id}/email")
    @Operation(summary = "Trigger sending invoice billing email notification")
    public ResponseEntity<String> emailInvoice(@PathVariable("id") UUID id) {
        invoiceService.emailInvoice(id);
        return ResponseEntity.ok("Invoice email sent successfully");
    }
}
