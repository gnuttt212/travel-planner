package com.travelplanner.recommendation.port;

import com.travelplanner.recommendation.domain.Destination;
import java.util.List;

/**
 * Port for searching destinations from external sources (Overpass/OSM, Google Places, etc.).
 * <p>
 * Implementations must handle rate limiting and graceful fallback internally.
 * On failure, implementations should return an empty list — never throw exceptions
 * to the caller.
 */
public interface DestinationSearchPort {

    /**
     * Search for POIs near a given coordinate within a radius.
     *
     * @param lat       center latitude
     * @param lon       center longitude
     * @param radiusKm  search radius in kilometers
     * @param city      city name (for context/tagging on returned entities)
     * @return list of Destination entities ready to persist; empty list on failure (never null)
     */
    List<Destination> searchNearby(double lat, double lon, double radiusKm, String city);
}
