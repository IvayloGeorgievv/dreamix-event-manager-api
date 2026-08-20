package org.example.eventmanagementapi.building;

import org.example.eventmanagementapi.building.dto.BuildingRequestDTO;
import org.example.eventmanagementapi.building.dto.BuildingResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
interface BuildingMapper {

    BuildingResponseDTO toResponseDTO(Building building);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateBuildingFromDto(BuildingRequestDTO buildingRequestDTO, @MappingTarget Building building);
}