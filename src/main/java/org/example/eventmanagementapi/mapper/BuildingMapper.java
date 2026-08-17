package org.example.eventmanagementapi.mapper;

import org.example.eventmanagementapi.dto.building.BuildingRequestDTO;
import org.example.eventmanagementapi.dto.building.BuildingResponseDTO;
import org.example.eventmanagementapi.model.Building;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;

@Mapper(componentModel = "spring")
public interface BuildingMapper {

    BuildingResponseDTO toResponseDTO(Building building);


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "venues", ignore = true)
    void updateBuildingFromDto(BuildingRequestDTO buildingRequestDTO, @MappingTarget Building building);
}