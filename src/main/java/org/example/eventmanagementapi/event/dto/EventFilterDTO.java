package org.example.eventmanagementapi.event.dto;

import org.example.eventmanagementapi.event.EventCategory;

public record EventFilterDTO(
        EventCategory category,
        String city,
        String search,
        Boolean onlyAvailable,
        Boolean upcomingOnly,
        Boolean includeDeleted
) {
}
