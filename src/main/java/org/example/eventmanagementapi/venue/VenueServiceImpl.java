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
    public VenueResponseDTO createVenue(final VenueRequestDTO request) {
        final Building building = buildingService.getBuildingEntityById(request.buildingId());

        final Venue venue = venueMapper.toEntity(request);
        venue.setBuilding(building);
        final Venue savedVenue = venueRepository.save(venue);

        return venueMapper.toResponseDTO(savedVenue);
    }

    @Override
    public VenueResponseDTO getVenueById(final UUID venueId) {
        return venueMapper.toResponseDTO(getVenueEntityById(venueId));
    }

    @Override
    public Venue getVenueEntityById(final UUID venueId) {
        return venueRepository.findByIdAndDeletedFalse(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Active venue not found with ID: " + venueId));
    }

    @Override
    public List<VenueResponseDTO> getAllVenues(final boolean includeDeleted) {
        final List<Venue> venues = includeDeleted
                ? venueRepository.findAll()
                : venueRepository.findAllByDeletedFalse();

        return venues.stream()
                .map(venueMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional
    public VenueResponseDTO updateVenue(final UUID venueId, final VenueRequestDTO request) {
        final Venue venue = getVenueEntityById(venueId);

        validateBuildingUnchanged(venue.getBuilding().getId(), request.buildingId());
        venueMapper.updateVenueFromDto(request, venue);

        return venueMapper.toResponseDTO(venue);
    }

    @Override
    @Transactional
    public void softDeleteVenue(final UUID venueId) {
        final Venue venue = getVenueEntityById(venueId);
        venue.setDeleted(true);
    }

    @Override
    @Transactional
    public void hardDeleteVenue(final UUID venueId) {
        validateVenueExists(venueId);
        venueRepository.deleteById(venueId);
    }

    @Override
    @Transactional
    public VenueResponseDTO restoreVenue(final UUID venueId) {
        final Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with ID: " + venueId));

        if (!venue.isDeleted()) {
            throw new BusinessLogicException("Venue is not deleted, nothing to restore!");
        }

        venue.setDeleted(false);
        return venueMapper.toResponseDTO(venue);
    }

    private void validateVenueExists(final UUID venueId) {
        if (!venueRepository.existsById(venueId)) {
            throw new ResourceNotFoundException("Venue not found");
        }
    }

    // Helper validation methods
    private void validateBuildingUnchanged(final UUID currentBuildingId, final UUID requestedBuildingId) {
        if (!currentBuildingId.equals(requestedBuildingId)) {
            throw new BusinessLogicException("Cannot change the building of an existing venue!");
        }
    }
}
