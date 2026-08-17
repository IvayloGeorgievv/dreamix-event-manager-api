package org.example.eventmanagementapi.mapper;

import org.example.eventmanagementapi.dto.event.EventRequestDTO;
import org.example.eventmanagementapi.dto.event.EventResponseDTO;
import org.example.eventmanagementapi.dto.event.EventSummaryResponseDTO;
import org.example.eventmanagementapi.model.Event;
import org.example.eventmanagementapi.model.Performer;
import org.example.eventmanagementapi.model.Venue;
import org.mapstruct.*;


@Mapper(componentModel = "spring")
public interface EventMapper {

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
