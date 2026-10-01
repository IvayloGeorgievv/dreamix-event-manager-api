package org.example.eventmanagementapi.building;

import lombok.RequiredArgsConstructor;
import org.example.eventmanagementapi.building.dto.BuildingRequestDTO;
import org.example.eventmanagementapi.building.dto.BuildingResponseDTO;
import org.example.eventmanagementapi.common.exception.BusinessLogicException;
import org.example.eventmanagementapi.common.exception.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BuildingServiceImpl implements BuildingService {

    private final BuildingRepository buildingRepository;

    private final BuildingMapper buildingMapper;

    @Override
    @Transactional
    public BuildingResponseDTO createBuilding(final BuildingRequestDTO request) {
        final Building building = buildingMapper.toEntity(request);
        final Building savedBuilding = buildingRepository.save(building);
        return buildingMapper.toResponseDTO(savedBuilding);
    }


    @Override
    public BuildingResponseDTO getBuildingById(final UUID buildingId) {
        return buildingMapper.toResponseDTO(getBuildingEntityById(buildingId));
    }


    @Override
    public Building getBuildingEntityById(final UUID buildingId) {
        return buildingRepository.findByIdAndDeletedFalse(buildingId)
                .orElseThrow(() -> new ResourceNotFoundException("Active building not found with ID: " + buildingId));
    }


    @Override
    public Page<BuildingResponseDTO> getBuildings(
            final String search,
            final boolean includeDeleted,
            final Pageable pageable
    ) {
        final boolean effectiveIncludeDeleted = includeDeleted && isCurrentUserAdmin();
        final String sanitizedSearch = search != null ? search.trim() : "";

        return buildingRepository.findBuildings(sanitizedSearch, effectiveIncludeDeleted, pageable)
                .map(buildingMapper::toResponseDTO);
    }


    @Override
    @Transactional
    public BuildingResponseDTO updateBuilding(final UUID buildingId, final BuildingRequestDTO request) {
        final Building building = getBuildingEntityById(buildingId);
        buildingMapper.updateBuildingFromDto(request, building);
        return buildingMapper.toResponseDTO(building);
    }


    @Override
    @Transactional
    public void softDeleteBuilding(final UUID buildingId) {
        final Building building = getBuildingEntityById(buildingId);
        building.setDeleted(true);
    }


    @Override
    @Transactional
    public void hardDeleteBuilding(final UUID buildingId) {
        validateBuildingExists(buildingId);
        buildingRepository.deleteById(buildingId);
    }


    @Override
    @Transactional
    public BuildingResponseDTO restoreBuilding(final UUID buildingId) {
        final Building building = buildingRepository.findById(buildingId)
                .orElseThrow(() -> new ResourceNotFoundException("Building not found with ID: " + buildingId));

        if (!building.isDeleted()) {
            throw new BusinessLogicException("Building is not deleted, nothing to restore!");
        }

        building.setDeleted(false);
        return buildingMapper.toResponseDTO(building);
    }

    //Helper methods
    private void validateBuildingExists(final UUID buildingId) {
        if (!buildingRepository.existsById(buildingId)) {
            throw new ResourceNotFoundException("Building not found with ID: " + buildingId);
        }
    }

    private boolean isCurrentUserAdmin() {
        final Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
    }
}