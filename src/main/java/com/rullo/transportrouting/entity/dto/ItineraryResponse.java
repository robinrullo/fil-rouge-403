package com.rullo.transportrouting.entity.dto;

import org.locationtech.jts.geom.LineString;

/**
 * Itinerary response.
 *
 * @param itinerary The itinerary.
 */
public record ItineraryResponse(LineString itineraryFeature, double distance, double time) {

}
