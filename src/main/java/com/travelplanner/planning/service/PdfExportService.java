package com.travelplanner.planning.service;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfWriter;
import com.travelplanner.planning.domain.Trip;
import com.travelplanner.planning.domain.TripActivity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

@Service
@Slf4j
public class PdfExportService {

    // Đường dẫn font Unicode trong resources, cần hỗ trợ dấu tiếng Việt.
    // Đặt file .ttf tại: src/main/resources/fonts/DejaVuSans.ttf
    // (có thể thay bằng font khác như NotoSans-Regular.ttf, Arial Unicode, v.v.)
    private static final String FONT_RESOURCE_PATH = "fonts/DejaVuSans.ttf";

    public byte[] exportToPdf(Trip trip) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            // Font Helvetica mặc định của OpenPDF/iText dùng encoding WinAnsi,
            // KHÔNG hiển thị được dấu tiếng Việt (ơ, ư, đ, ...).
            // Phải nhúng font TTF Unicode với encoding IDENTITY_H để hiển thị đúng.
            BaseFont baseFont = loadUnicodeBaseFont();
            Font titleFont = new Font(baseFont, 18, Font.BOLD);
            Font subFont = new Font(baseFont, 12, Font.BOLD);
            Font bodyFont = new Font(baseFont, 11, Font.NORMAL);

            Paragraph title = new Paragraph("Chuyến đi: " + trip.getTitle(), titleFont);
            title.setSpacingAfter(10);
            document.add(title);

            Paragraph dates = new Paragraph(
                    String.format("Ngày đi: %s", trip.getTripDate()),
                    bodyFont
            );
            dates.setSpacingAfter(20);
            document.add(dates);

            Paragraph activitiesHeader = new Paragraph("Lịch trình chi tiết:", subFont);
            activitiesHeader.setSpacingAfter(8);
            document.add(activitiesHeader);

            if (trip.getActivities() == null || trip.getActivities().isEmpty()) {
                document.add(new Paragraph("Chưa có hoạt động nào được lên lịch.", bodyFont));
            } else {
                BigDecimal totalCost = BigDecimal.ZERO;
                for (TripActivity act : trip.getActivities()) {
                    String line = String.format(
                            "%s - %s: %s (Chi phí: %sđ)",
                            act.getPlannedStartTime(),
                            act.getPlannedEndTime(),
                            act.getDestinationName(),
                            act.getEstimatedCost().toPlainString()
                    );
                    Paragraph activityLine = new Paragraph(line, bodyFont);
                    activityLine.setSpacingAfter(4);
                    document.add(activityLine);
                    totalCost = totalCost.add(act.getEstimatedCost());
                }

                Paragraph total = new Paragraph(
                    String.format("Tổng chi phí dự kiến: %sđ", totalCost.toPlainString()),
                    subFont
                );
                total.setSpacingBefore(12);
                document.add(total);
            }

            document.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Error generating PDF", e);
            throw new RuntimeException("Could not generate PDF");
        }
    }

    /**
     * Nạp font Unicode từ classpath và nhúng (embed) vào PDF với encoding IDENTITY_H.
     * Bắt buộc dùng cách này thay vì FontFactory.getFont(HELVETICA...) để hiển thị
     * đúng ký tự có dấu tiếng Việt.
     */
    private BaseFont loadUnicodeBaseFont() throws IOException, DocumentException {
        ClassPathResource resource = new ClassPathResource(FONT_RESOURCE_PATH);
        byte[] fontBytes = StreamUtils.copyToByteArray(resource.getInputStream());
        return BaseFont.createFont(
                "DejaVuSans.ttf",
                BaseFont.IDENTITY_H,
                BaseFont.EMBEDDED,
                true,
                fontBytes,
                null
        );
    }
}