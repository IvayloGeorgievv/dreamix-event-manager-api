package org.example.eventmanagementapi.venue;

import org.example.eventmanagementapi.venue.dto.VenueRequestDTO;
import org.example.eventmanagementapi.venue.dto.VenueResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface VenueService {

    VenueResponseDTO createVenue(VenueRequestDTO request);

    VenueResponseDTO getVenueById(UUID venueId);

    Venue getVenueEntityById(UUID venueId);

    Page<VenueResponseDTO> getVenues(String search, boolean includeDeleted, Pageable pageable);

    VenueResponseDTO updateVenue(UUID venueId, VenueRequestDTO request);

    void softDeleteVenue(UUID venueId);

    void hardDeleteVenue(UUID venueId);

    VenueResponseDTO restoreVenue(UUID venueId);
}