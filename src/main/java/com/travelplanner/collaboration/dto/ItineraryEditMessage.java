package com.travelplanner.collaboration.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Message duoc gui qua STOMP khi mot thanh vien chinh sua lich trinh.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItineraryEditMessage {
    private String itineraryId;
    private String userId;
    private String action;   // ADD, UPDATE, DELETE, REORDER
    private String entityType; // DESTINATION, EXPENSE, NOTE
    private String entityId;
    private String payload;  // JSON string of the change
}
