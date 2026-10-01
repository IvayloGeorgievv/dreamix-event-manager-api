package org.example.eventmanagementapi.event;

import org.example.eventmanagementapi.event.dto.EventRequestDTO;
import org.example.eventmanagementapi.event.dto.EventResponseDTO;
import org.example.eventmanagementapi.event.dto.EventSummaryResponseDTO;
import org.example.eventmanagementapi.event.dto.ShowScheduleDTO;
import org.example.eventmanagementapi.performer.Performer;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
interface EventMapper {

    @Mapping(target = "venueId", source = "event.venue.id")
    @Mapping(target = "venueName", source = "event.venue.name")
    @Mapping(target = "venueAddress", source = "event.venue.building.address")
    @Mapping(target = "venueCapacity", source = "event.venue.capacity")
    @Mapping(target = "cityName", source = "event.venue.building.city")
    @Mapping(target = "performerNames", source = "event.performers")
    @Mapping(target = "schedules", source = "schedules")
    EventResponseDTO toResponseDTO(Event event, List<ShowScheduleDTO> schedules);

    @Mapping(target = "venueId", source = "venue.id")
    @Mapping(target = "venueName", source = "venue.name")
    @Mapping(target = "cityName", source = "venue.building.city")
    @Mapping(target = "performerNames", source = "performers")
    EventSummaryResponseDTO toSummaryResponseDTO(Event event);

    @Mapping(target = "city", source = "venue.building.city")
    @Mapping(target = "venueId", source = "venue.id")
    @Mapping(target = "venueName", source = "venue.name")
    @Mapping(target = "venueAddress", source = "venue.building.address")
    @Mapping(target = "startingPrice", source = "basePrice")
    ShowScheduleDTO toScheduleDTO(Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "venue", ignore = true)
    @Mapping(target = "performers", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    void updateEventFromDto(EventRequestDTO eventRequestDTO, @MappingTarget Event event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "venue", ignore = true)
    @Mapping(target = "performers", ignore = true)
    @Mapping(target = "tickets", ignore = true)
    Event toEntity(EventRequestDTO eventRequestDTO);

    default String performerToName(Performer performer) {
        return performer.getName();
    }
}
