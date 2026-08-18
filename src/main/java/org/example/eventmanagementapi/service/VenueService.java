package org.example.eventmanagementapi.service;

import lombok.RequiredArgsConstructor;
import org.example.eventmanagementapi.dto.venue.VenueRequestDTO;
import org.example.eventmanagementapi.dto.venue.VenueResponseDTO;
import org.example.eventmanagementapi.exception.BusinessLogicException;
import org.example.eventmanagementapi.exception.ResourceNotFoundException;
import org.example.eventmanagementapi.mapper.VenueMapper;
import org.example.eventmanagementapi.model.Building;
import org.example.eventmanagementapi.model.Venue;
import org.example.eventmanagementapi.repository.VenueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VenueService {

    private final VenueRepository venueRepository;

    private final BuildingService buildingService;

    private final VenueMapper venueMapper;

    @Transactional
    public VenueResponseDTO createVenue(VenueRequestDTO request) {
        Building building = buildingService.getBuildingEntityById(request.buildingId());

        Venue venue = new Venue(request.name(), request.capacity(), building);
        building.addVenue(venue);

        Venue savedVenue = venueRepository.save(venue);
        return venueMapper.toResponseDTO(savedVenue);
    }

    public VenueResponseDTO getVenueById(UUID venueId) {
        return venueMapper.toResponseDTO(getVenueEntityById(venueId));
    }

    protected Venue getVenueEntityById(UUID venueId) {
        return venueRepository.findByIdAndDeletedFalse(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Active venue not found with ID: " + venueId));
    }

    public List<VenueResponseDTO> getAllVenues(boolean includeDeleted) {
        List<Venue> venues = includeDeleted
                ? venueRepository.findAll()
                : venueRepository.findAllByDeletedFalse();

        return venues.stream()
                .map(venueMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public VenueResponseDTO updateVenue(UUID venueId, VenueRequestDTO request) {
        Venue venue = getVenueEntityById(venueId);

        validateBuildingUnchanged(venue.getBuilding().getId(), request.buildingId());
        venueMapper.updateVenueFromDto(request, venue);

        return venueMapper.toResponseDTO(venue);
    }

    @Transactional
    public void softDeleteVenue(UUID venueId) {
        Venue venue = getVenueEntityById(venueId);
        venue.setDeleted(true);
    }

    @Transactional
    public void hardDeleteVenue(UUID venueId) {
        validateVenueExists(venueId);
        venueRepository.deleteById(venueId);
    }

    @Transactional
    public VenueResponseDTO restoreVenue(UUID venueId) {
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with ID: " + venueId));

        if (!venue.isDeleted()) {
            throw new BusinessLogicException("Venue is not deleted, nothing to restore!");
        }

        venue.setDeleted(false);
        return venueMapper.toResponseDTO(venue);
    }

    public void validateVenueExists(UUID venueId) {
        if (!venueRepository.existsById(venueId)) {
            throw new ResourceNotFoundException("Venue not found");
        }
    }

    // Helper validation methods
    private void validateBuildingUnchanged(UUID currentBuildingId, UUID requestedBuildingId) {
        if (!currentBuildingId.equals(requestedBuildingId)) {
            throw new BusinessLogicException("Cannot change the building of an existing venue!");
        }
    }
}
