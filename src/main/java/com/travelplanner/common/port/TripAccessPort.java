package com.travelplanner.common.port;

public interface TripAccessPort {
    boolean canView(String tripId, String userEmail);
    boolean isOwner(String tripId, String userEmail);
}
