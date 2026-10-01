package org.example.eventmanagementapi.building;

import org.example.eventmanagementapi.building.dto.BuildingRequestDTO;
import org.example.eventmanagementapi.building.dto.BuildingResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface BuildingService {

    BuildingResponseDTO createBuilding(BuildingRequestDTO request);

    BuildingResponseDTO getBuildingById(UUID buildingId);

    Building getBuildingEntityById(UUID buildingId);

    Page<BuildingResponseDTO> getBuildings(String search, boolean includeDeleted, Pageable pageable);

    BuildingResponseDTO updateBuilding(UUID buildingId, BuildingRequestDTO request);

    void softDeleteBuilding(UUID buildingId);

    void hardDeleteBuilding(UUID buildingId);

    BuildingResponseDTO restoreBuilding(UUID buildingId);
}
