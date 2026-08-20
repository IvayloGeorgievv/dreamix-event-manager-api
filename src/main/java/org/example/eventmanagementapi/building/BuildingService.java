package org.example.eventmanagementapi.building;

import org.example.eventmanagementapi.building.dto.BuildingRequestDTO;
import org.example.eventmanagementapi.building.dto.BuildingResponseDTO;

import java.util.List;
import java.util.UUID;

public interface BuildingService {
    BuildingResponseDTO createBuilding(BuildingRequestDTO request);

    BuildingResponseDTO getBuildingById(UUID buildingId);

    Building getBuildingEntityById(UUID buildingId);

    List<BuildingResponseDTO> getAllBuildings(boolean includeDeleted);

    BuildingResponseDTO updateBuilding(UUID buildingId, BuildingRequestDTO request);

    void softDeleteBuilding(UUID buildingId);

    void hardDeleteBuilding(UUID buildingId);

    BuildingResponseDTO restoreBuilding(UUID buildingId);
}
