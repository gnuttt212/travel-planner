package com.travelplanner.planning.service;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import com.travelplanner.planning.domain.Trip;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
@Slf4j
public class PdfExportService {

    public byte[] exportToPdf(Trip trip) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font bodyFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            Paragraph title = new Paragraph("Trip: " + trip.getTitle(), titleFont);
            title.setSpacingAfter(10);
            document.add(title);

            Paragraph dates = new Paragraph(
                    String.format("Date: %s", trip.getTripDate()),
                    bodyFont
            );
            dates.setSpacingAfter(20);
            document.add(dates);

            String content = "Activities: " + (trip.getActivities() != null ? trip.getActivities().size() : 0);

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
