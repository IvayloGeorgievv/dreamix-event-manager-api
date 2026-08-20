package org.example.eventmanagementapi.venue;

import org.example.eventmanagementapi.venue.dto.VenueRequestDTO;
import org.example.eventmanagementapi.venue.dto.VenueResponseDTO;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/venues")
@RequiredArgsConstructor
public class VenueController {

    private final VenueService venueService;

    @PostMapping
    public ResponseEntity<VenueResponseDTO> createVenue(@Valid @RequestBody VenueRequestDTO request) {
        VenueResponseDTO created = venueService.createVenue(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VenueResponseDTO> getVenueById(@PathVariable UUID id) {
        return ResponseEntity.ok(venueService.getVenueById(id));
    }

    @GetMapping
    public ResponseEntity<List<VenueResponseDTO>> getAllVenues(
            @RequestParam(name = "includeDeleted", defaultValue = "false") boolean includeDeleted) {
        return ResponseEntity.ok(venueService.getAllVenues(includeDeleted));
    }

    @PutMapping("/{id}")
    public ResponseEntity<VenueResponseDTO> updateVenue(@PathVariable UUID id, @Valid @RequestBody VenueRequestDTO request) {
        return ResponseEntity.ok(venueService.updateVenue(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVenue(@PathVariable UUID id) {

        venueService.softDeleteVenue(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Void> hardDeleteVenue(@PathVariable UUID id) {

        venueService.hardDeleteVenue(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/restore")
    public ResponseEntity<VenueResponseDTO> restoreVenue(@PathVariable UUID id) {
        return ResponseEntity.ok(venueService.restoreVenue(id));
    }
}
