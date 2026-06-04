package com.saas.billing.serviceImpl;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.saas.billing.dto.InvoiceDto;
import com.saas.billing.entity.*;
import com.saas.billing.exception.ResourceNotFoundException;
import com.saas.billing.mapper.DtoMapper;
import com.saas.billing.repository.InvoiceRepository;
import com.saas.billing.repository.SettingsRepository;
import com.saas.billing.service.InvoiceService;
import com.saas.billing.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final SettingsRepository settingsRepository;
    private final NotificationService notificationService;

    @Value("${app.tax.home-state}")
    private String homeState;

    @Value("${app.tax.cgst-rate}")
    private double cgstRate;

    @Value("${app.tax.sgst-rate}")
    private double sgstRate;

    @Value("${app.tax.igst-rate}")
    private double igstRate;

    @Override
    public List<InvoiceDto> getInvoices(UUID organizationId) {
        return invoiceRepository.findByOrganizationIdOrderByCreatedAtDesc(organizationId).stream()
                .map(DtoMapper::toInvoiceDto)
                .collect(Collectors.toList());
    }

    @Override
    public InvoiceDto getInvoiceById(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
        return DtoMapper.toInvoiceDto(invoice);
    }

    @Override
    @Transactional
    public ByteArrayInputStream generatePdfInvoice(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

        // Load organization settings for GST calculations
        Settings settings = settingsRepository.findByOrganizationId(invoice.getOrganization().getId())
                .orElse(Settings.builder().country("US").build());

        // Perform tax calculations before rendering
        calculateTaxes(invoice, settings);
        invoiceRepository.save(invoice);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Set Premium Fonts and Colors
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 22, Color.DARK_GRAY);
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.BLACK);
            Font regularFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);

            // Document Header
            Paragraph docTitle = new Paragraph("INVOICE", titleFont);
            docTitle.setAlignment(Element.ALIGN_RIGHT);
            document.add(docTitle);
            document.add(new Paragraph(" "));

            // Two-column layout for Organization Details vs. Invoice metadata
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);

            // Left Cell: Organization Info
            PdfPCell leftCell = new PdfPCell();
            leftCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
            leftCell.addElement(new Paragraph("Billed To:", boldFont));
            leftCell.addElement(new Paragraph(invoice.getOrganization().getName(), regularFont));
            leftCell.addElement(new Paragraph("Billing Email: " + (settings.getBillingEmail() != null ? settings.getBillingEmail() : invoice.getOrganization().getSlug() + "@company.com"), regularFont));
            leftCell.addElement(new Paragraph("Country: " + settings.getCountry(), regularFont));
            if (settings.getGstin() != null) {
                leftCell.addElement(new Paragraph("GSTIN: " + settings.getGstin(), regularFont));
            }
            infoTable.addCell(leftCell);

            // Right Cell: Invoice Meta
            PdfPCell rightCell = new PdfPCell();
            rightCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
            rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            rightCell.addElement(new Paragraph("Invoice Number: " + invoice.getInvoiceNumber(), boldFont));
            rightCell.addElement(new Paragraph("Date: " + invoice.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), regularFont));
            rightCell.addElement(new Paragraph("Due Date: " + invoice.getDueDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), regularFont));
            rightCell.addElement(new Paragraph("Status: " + invoice.getStatus(), boldFont));
            infoTable.addCell(rightCell);

            document.add(infoTable);
            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            // Invoice Line Items Table
            PdfPTable itemsTable = new PdfPTable(4);
            itemsTable.setWidthPercentage(100);
            itemsTable.setWidths(new float[]{4, 1, 2, 2});

            // Headers
            Color headerBg = new Color(79, 70, 229); // Premium Indigo
            PdfPCell h1 = new PdfPCell(new Phrase("Description", headerFont)); h1.setBackgroundColor(headerBg); itemsTable.addCell(h1);
            PdfPCell h2 = new PdfPCell(new Phrase("Qty", headerFont)); h2.setBackgroundColor(headerBg); itemsTable.addCell(h2);
            PdfPCell h3 = new PdfPCell(new Phrase("Unit Price", headerFont)); h3.setBackgroundColor(headerBg); itemsTable.addCell(h3);
            PdfPCell h4 = new PdfPCell(new Phrase("Amount", headerFont)); h4.setBackgroundColor(headerBg); itemsTable.addCell(h4);

            for (InvoiceItem item : invoice.getItems()) {
                itemsTable.addCell(new PdfPCell(new Phrase(item.getDescription(), regularFont)));
                itemsTable.addCell(new PdfPCell(new Phrase(String.valueOf(item.getQuantity()), regularFont)));
                itemsTable.addCell(new PdfPCell(new Phrase("$" + item.getUnitAmount().toString(), regularFont)));
                itemsTable.addCell(new PdfPCell(new Phrase("$" + item.getAmount().toString(), regularFont)));
            }

            document.add(itemsTable);
            document.add(new Paragraph(" "));

            // Summary Totals
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(50);
            summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);

            summaryTable.addCell(new PdfPCell(new Phrase("Subtotal", regularFont)));
            summaryTable.addCell(new PdfPCell(new Phrase("$" + invoice.getSubtotal().toString(), regularFont)));

            if (invoice.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
                summaryTable.addCell(new PdfPCell(new Phrase("Discount", regularFont)));
                summaryTable.addCell(new PdfPCell(new Phrase("-$" + invoice.getDiscountAmount().toString(), regularFont)));
            }

            if (invoice.getCgst().compareTo(BigDecimal.ZERO) > 0) {
                summaryTable.addCell(new PdfPCell(new Phrase("CGST (" + cgstRate + "%)", regularFont)));
                summaryTable.addCell(new PdfPCell(new Phrase("$" + invoice.getCgst().toString(), regularFont)));
            }

            if (invoice.getSgst().compareTo(BigDecimal.ZERO) > 0) {
                summaryTable.addCell(new PdfPCell(new Phrase("SGST (" + sgstRate + "%)", regularFont)));
                summaryTable.addCell(new PdfPCell(new Phrase("$" + invoice.getSgst().toString(), regularFont)));
            }

            if (invoice.getIgst().compareTo(BigDecimal.ZERO) > 0) {
                summaryTable.addCell(new PdfPCell(new Phrase("IGST (" + igstRate + "%)", regularFont)));
                summaryTable.addCell(new PdfPCell(new Phrase("$" + invoice.getIgst().toString(), regularFont)));
            }

            summaryTable.addCell(new PdfPCell(new Phrase("Total", boldFont)));
            summaryTable.addCell(new PdfPCell(new Phrase("$" + invoice.getTotal().toString(), boldFont)));

            document.add(summaryTable);
            document.close();

        } catch (DocumentException de) {
            log.error("Failed to generate PDF invoice: {}", de.getMessage());
        }

        return new ByteArrayInputStream(out.toByteArray());
    }

    private void calculateTaxes(Invoice invoice, Settings settings) {
        BigDecimal subtotal = invoice.getSubtotal().subtract(invoice.getDiscountAmount());
        if (subtotal.compareTo(BigDecimal.ZERO) < 0) {
            subtotal = BigDecimal.ZERO;
        }

        // Determine if GST applies based on country and state
        if ("IN".equalsIgnoreCase(settings.getCountry()) || settings.getGstin() != null) {
            // Apply standard Indian GST rule:
            // Compare organization state with home state. For simplicity, we check if GSTIN starts with homeState code or homeState parameter match.
            boolean isIntraState = settings.getGstin() != null && settings.getGstin().startsWith("27"); // MH code example

            if (isIntraState) {
                BigDecimal cgst = subtotal.multiply(BigDecimal.valueOf(cgstRate / 100.0)).setScale(2, RoundingMode.HALF_UP);
                BigDecimal sgst = subtotal.multiply(BigDecimal.valueOf(sgstRate / 100.0)).setScale(2, RoundingMode.HALF_UP);
                invoice.setCgst(cgst);
                invoice.setSgst(sgst);
                invoice.setIgst(BigDecimal.ZERO);
                invoice.setTaxAmount(cgst.add(sgst));
            } else {
                BigDecimal igst = subtotal.multiply(BigDecimal.valueOf(igstRate / 100.0)).setScale(2, RoundingMode.HALF_UP);
                invoice.setCgst(BigDecimal.ZERO);
                invoice.setSgst(BigDecimal.ZERO);
                invoice.setIgst(igst);
                invoice.setTaxAmount(igst);
            }
        } else {
            // Standard country taxation (e.g. 5% global sales tax)
            BigDecimal globalTax = subtotal.multiply(BigDecimal.valueOf(0.05)).setScale(2, RoundingMode.HALF_UP);
            invoice.setTaxAmount(globalTax);
            invoice.setCgst(BigDecimal.ZERO);
            invoice.setSgst(BigDecimal.ZERO);
            invoice.setIgst(BigDecimal.ZERO);
        }

        invoice.setTotal(subtotal.add(invoice.getTaxAmount()).setScale(2, RoundingMode.HALF_UP));
    }

    @Override
    public void emailInvoice(UUID invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));

        Settings settings = settingsRepository.findByOrganizationId(invoice.getOrganization().getId())
                .orElse(Settings.builder().billingEmail(invoice.getOrganization().getSlug() + "@company.com").build());

        String email = settings.getBillingEmail();
        String subject = "Your Invoice " + invoice.getInvoiceNumber() + " is ready";
        String body = "Dear customer,\n\nYour invoice " + invoice.getInvoiceNumber() + " has been generated.\n"
                + "Total Due: $" + invoice.getTotal().toString() + "\n"
                + "Billing Reason: " + invoice.getBillingReason() + "\n\n"
                + "Thank you for your support!\nSaaS Billing Platform Team";

        // Dispatch email notification
        notificationService.sendEmail(email, subject, body);
        log.info("Sent invoice notification email to: {}", email);
    }
}
