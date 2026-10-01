package org.example.eventmanagementapi.event;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum EventCategory {
    THEATRE,
    CONCERTS,
    MUSICALS;

    @JsonCreator
    public static EventCategory fromString(String value) {
        if (value == null) {
            return null;
        }
        return EventCategory.valueOf(value.trim().toUpperCase());
    }
}
