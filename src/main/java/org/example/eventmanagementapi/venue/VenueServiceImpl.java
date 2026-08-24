package org.example.eventmanagementapi.venue;

import lombok.RequiredArgsConstructor;
import org.example.eventmanagementapi.building.BuildingService;
import org.example.eventmanagementapi.common.exception.BusinessLogicException;
import org.example.eventmanagementapi.common.exception.ResourceNotFoundException;
import org.example.eventmanagementapi.building.Building;
import org.example.eventmanagementapi.venue.dto.VenueRequestDTO;
import org.example.eventmanagementapi.venue.dto.VenueResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VenueServiceImpl implements VenueService {

    private final VenueRepository venueRepository;

    private final BuildingService buildingService;

    private final VenueMapper venueMapper;

    @Override
    @Transactional
    public VenueResponseDTO createVenue(VenueRequestDTO request) {
        Building building = buildingService.getBuildingEntityById(request.buildingId());

        Venue venue = venueMapper.toEntity(request);
        venue.setBuilding(building);
        Venue savedVenue = venueRepository.save(venue);

        return venueMapper.toResponseDTO(savedVenue);
    }

    @Override
    public VenueResponseDTO getVenueById(UUID venueId) {
        return venueMapper.toResponseDTO(getVenueEntityById(venueId));
    }

    @Override
    public Venue getVenueEntityById(UUID venueId) {
        return venueRepository.findByIdAndDeletedFalse(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Active venue not found with ID: " + venueId));
    }

    @Override
    public List<VenueResponseDTO> getAllVenues(boolean includeDeleted) {
        List<Venue> venues = includeDeleted
                ? venueRepository.findAll()
                : venueRepository.findAllByDeletedFalse();

        return venues.stream()
                .map(venueMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public VenueResponseDTO updateVenue(UUID venueId, VenueRequestDTO request) {
        Venue venue = getVenueEntityById(venueId);

        validateBuildingUnchanged(venue.getBuilding().getId(), request.buildingId());
        venueMapper.updateVenueFromDto(request, venue);

        return venueMapper.toResponseDTO(venue);
    }

    @Override
    @Transactional
    public void softDeleteVenue(UUID venueId) {
        Venue venue = getVenueEntityById(venueId);
        venue.setDeleted(true);
    }

    @Override
    @Transactional
    public void hardDeleteVenue(UUID venueId) {
        validateVenueExists(venueId);
        venueRepository.deleteById(venueId);
    }

    @Override
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

    private void validateVenueExists(UUID venueId) {
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
