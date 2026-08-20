package org.example.eventmanagementapi.event;

import org.example.eventmanagementapi.event.dto.EventRequestDTO;
import org.example.eventmanagementapi.event.dto.EventResponseDTO;
import org.example.eventmanagementapi.event.dto.EventSummaryResponseDTO;
import org.example.eventmanagementapi.performer.Performer;
import org.mapstruct.*;


@Mapper(componentModel = "spring")
interface EventMapper {

    @Mapping(target = "venueId", source = "venue.id")
    @Mapping(target = "venueName", source = "venue.name")
    @Mapping(target = "cityName", source = "venue.building.city")
    @Mapping(target = "performerNames", source = "performers")
    EventResponseDTO toResponseDTO(Event event);

    @Mapping(target = "venueId", source = "venue.id")
    @Mapping(target = "venueName", source = "venue.name")
    @Mapping(target = "cityName", source = "venue.building.city")
    @Mapping(target = "performerNames", source = "performers")
    EventSummaryResponseDTO toSummaryResponseDTO(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEventFromDto(EventRequestDTO eventRequestDTO, @MappingTarget Event event);

    default String performerToName(Performer performer) {
        return performer.getName();
    }
}
