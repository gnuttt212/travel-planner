package com.travelplanner.itinerary.service;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.travelplanner.itinerary.domain.Itinerary;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
@Slf4j
public class PdfExportService {

    public byte[] exportToPdf(Itinerary itinerary) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            Paragraph title = new Paragraph("Itinerary: " + itinerary.getTitle(), titleFont);
            title.setSpacingAfter(10);
            document.add(title);

            Paragraph dates = new Paragraph(
                    String.format("From: %s To: %s", itinerary.getStartDate(), itinerary.getEndDate()),
                    bodyFont
            );
            dates.setSpacingAfter(20);
            document.add(dates);

            String content = itinerary.getContent();
            if (content == null || content.isEmpty()) {
                content = "No details available for this itinerary.";
            }

            // Simple text rendering. In a real app, markdown parsing would be applied here.
            Paragraph body = new Paragraph(content, bodyFont);
            document.add(body);

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating PDF", e);
            throw new RuntimeException("Could not generate PDF");
        }
    }
}
