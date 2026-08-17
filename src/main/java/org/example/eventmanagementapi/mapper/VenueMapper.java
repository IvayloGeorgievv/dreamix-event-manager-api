package org.example.eventmanagementapi.mapper;

import org.example.eventmanagementapi.dto.venue.VenueRequestDTO;
import org.example.eventmanagementapi.dto.venue.VenueResponseDTO;
import org.example.eventmanagementapi.model.Building;
import org.example.eventmanagementapi.model.Venue;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface VenueMapper {

    @Mapping(target = "buildingId", source = "building.id")
    @Mapping(target = "buildingName", source = "building.name")
    @Mapping(target = "city", source = "building.city")
    @Mapping(target = "address", source = "building.address")
    VenueResponseDTO toResponseDTO(Venue venue);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateVenueFromDto(VenueRequestDTO venueRequestDTO, @MappingTarget Venue venue);
}
