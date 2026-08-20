package org.example.eventmanagementapi.venue;

import org.example.eventmanagementapi.venue.dto.VenueRequestDTO;
import org.example.eventmanagementapi.venue.dto.VenueResponseDTO;

import java.util.List;
import java.util.UUID;

public interface VenueService {

    VenueResponseDTO createVenue(VenueRequestDTO request);

    VenueResponseDTO getVenueById(UUID venueId);

    Venue getVenueEntityById(UUID venueId);

    List<VenueResponseDTO> getAllVenues(boolean includeDeleted);

    VenueResponseDTO updateVenue(UUID venueId, VenueRequestDTO request);

    void softDeleteVenue(UUID venueId);

    void hardDeleteVenue(UUID venueId);

    VenueResponseDTO restoreVenue(UUID venueId);
}