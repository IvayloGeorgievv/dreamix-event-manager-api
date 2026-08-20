package org.example.eventmanagementapi.venue;

import org.example.eventmanagementapi.venue.dto.VenueRequestDTO;
import org.example.eventmanagementapi.venue.dto.VenueResponseDTO;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
interface VenueMapper {

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
