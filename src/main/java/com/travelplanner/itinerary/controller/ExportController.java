package com.travelplanner.itinerary.controller;

import com.travelplanner.itinerary.domain.Itinerary;
import com.travelplanner.itinerary.repository.ItineraryRepository;
import com.travelplanner.itinerary.service.CalendarExportService;
import com.travelplanner.itinerary.service.PdfExportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/itineraries")
@RequiredArgsConstructor
public class ExportController {

    private final ItineraryRepository itineraryRepository;
    private final PdfExportService pdfExportService;
    private final CalendarExportService calendarExportService;

    @GetMapping("/{id}/export/pdf")
    public ResponseEntity<byte[]> exportPdf(@PathVariable String id) {
        Itinerary itinerary = itineraryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Itinerary not found"));

        byte[] pdfBytes = pdfExportService.exportToPdf(itinerary);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "itinerary_" + id + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/{id}/export/ics")
    public ResponseEntity<String> exportIcs(@PathVariable String id) {
        Itinerary itinerary = itineraryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Itinerary not found"));

        String icsContent = calendarExportService.exportToIcs(itinerary);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/calendar"));
        headers.setContentDispositionFormData("attachment", "itinerary_" + id + ".ics");

        return ResponseEntity.ok()
                .headers(headers)
                .body(icsContent);
    }
}
