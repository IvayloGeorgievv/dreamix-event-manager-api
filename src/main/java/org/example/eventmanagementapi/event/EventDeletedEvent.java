package org.example.eventmanagementapi.event;

import java.util.UUID;

public record EventDeletedEvent(
        UUID eventId,
        boolean hardDelete
) {
}
