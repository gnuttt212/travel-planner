package com.travelplanner.itinerary.service;

import com.travelplanner.itinerary.domain.Itinerary;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class CalendarExportService {

    /**
     * Tao chuoi .ics dung de import vao Google Calendar, Apple Calendar
     */
    public String exportToIcs(Itinerary itinerary) {
        StringBuilder sb = new StringBuilder();
        sb.append("BEGIN:VCALENDAR\n");
        sb.append("VERSION:2.0\n");
        sb.append("PRODID:-//TravelPlannerAI//EN\n");
        
        sb.append("BEGIN:VEVENT\n");
        sb.append("UID:").append(UUID.randomUUID().toString()).append("\n");
        
        // Format dates as YYYYMMDD
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");
        String startStr = itinerary.getStartDate().format(dtf);
        // End date in ICS is exclusive, so add 1 day
        String endStr = itinerary.getEndDate().plusDays(1).format(dtf);
        
        sb.append("DTSTAMP:").append(LocalDate.now().format(dtf)).append("T000000Z\n");
        sb.append("DTSTART;VALUE=DATE:").append(startStr).append("\n");
        sb.append("DTEND;VALUE=DATE:").append(endStr).append("\n");
        
        sb.append("SUMMARY:").append(itinerary.getTitle()).append(" Trip\n");
        
        String desc = itinerary.getContent() != null ? itinerary.getContent().replaceAll("\n", "\\\\n") : "Have a great trip!";
        // Basic escaping and truncating if too long for standard ICS parsers, but leaving simple here
        if (desc.length() > 500) {
            desc = desc.substring(0, 500) + "... (See app for full details)";
        }
        sb.append("DESCRIPTION:").append(desc).append("\n");
        
        sb.append("END:VEVENT\n");
        sb.append("END:VCALENDAR\n");
        
        return sb.toString();
    }
}
