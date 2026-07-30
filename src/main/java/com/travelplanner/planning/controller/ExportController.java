package com.travelplanner.planning.controller;

import com.travelplanner.common.exception.AccessDeniedException;
import com.travelplanner.planning.domain.Trip;
import com.travelplanner.planning.domain.TripActivity;
import com.travelplanner.planning.repository.TripRepository;
import com.travelplanner.planning.security.TripSecurity;
import com.travelplanner.planning.service.PdfExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
public class ExportController {

    private final TripRepository tripRepository;
    private final PdfExportService pdfExportService;
    private final TripSecurity tripSecurity;

    @GetMapping("/{id}/export/ics")
    public ResponseEntity<byte[]> exportIcs(@PathVariable UUID id, Authentication authentication) {
        // Ownership/visibility check trước khi export
        if (!tripSecurity.canView(id, authentication)) {
            throw AccessDeniedException.of("trip");
        }
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        String ics = buildIcsContent(trip);
        byte[] bytes = ics.getBytes(StandardCharsets.UTF_8);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/calendar"));
        headers.setContentDispositionFormData("attachment", "trip_" + id + ".ics");

        return ResponseEntity.ok().headers(headers).body(bytes);
    }

    /**
     * Endpoint bị thiếu trước đó: frontend (planningApi.exportPdf) gọi
     * GET /api/v1/trips/{id}/export/pdf với responseType: 'blob', nhưng backend
     * chưa có handler tương ứng -> sẽ trả về 404 Not Found khi gọi thực tế.
     */
    @GetMapping("/{id}/export/pdf")
    public ResponseEntity<byte[]> exportPdf(@PathVariable UUID id, Authentication authentication) {
        // Ownership/visibility check trước khi export
        if (!tripSecurity.canView(id, authentication)) {
            throw AccessDeniedException.of("trip");
        }
        Trip trip = tripRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Trip not found"));

        byte[] pdfBytes = pdfExportService.exportToPdf(trip);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "trip_" + id + ".pdf");
        // Ngăn trình duyệt/proxy cache file PDF export theo yêu cầu tức thời
        headers.setCacheControl("no-store");

        return ResponseEntity.ok().headers(headers).body(pdfBytes);
    }

    private String buildIcsContent(Trip trip) {
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR\r\n");
        sb.append("VERSION:2.0\r\n");
        sb.append("PRODID:-//TravelPlannerAI//Trip Export//VN\r\n");

        DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HHmmss");

        for (TripActivity act : trip.getActivities()) {
            String dtStart = trip.getTripDate().format(dateFmt) + "T" + act.getPlannedStartTime().format(timeFmt);
            String dtEnd = trip.getTripDate().format(dateFmt) + "T" + act.getPlannedEndTime().format(timeFmt);

            sb.append("BEGIN:VEVENT\r\n");
            sb.append("UID:").append(act.getId()).append("@travelplanner.ai\r\n");
            sb.append("DTSTART:").append(dtStart).append("\r\n");
            sb.append("DTEND:").append(dtEnd).append("\r\n");
            sb.append("SUMMARY:").append(escapeIcs(act.getDestinationName())).append("\r\n");
            sb.append("DESCRIPTION:").append(escapeIcs("Chi phí: " + act.getEstimatedCost() + "đ")).append("\r\n");
            sb.append("LOCATION:").append(escapeIcs(act.getDestinationName())).append("\r\n");
            sb.append("END:VEVENT\r\n");
        }

        sb.append("END:VCALENDAR\r\n");
        return sb.toString();
    }

    private String escapeIcs(String text) {
        return text.replace(",", "\\,").replace(";", "\\;");
    }
}