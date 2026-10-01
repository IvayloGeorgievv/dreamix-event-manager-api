package org.example.eventmanagementapi.event;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.example.eventmanagementapi.building.Building;
import org.example.eventmanagementapi.event.dto.EventFilterDTO;
import org.example.eventmanagementapi.venue.Venue;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public final class EventSpecification {

    private EventSpecification() {
    }

    public static Specification<Event> withFilter(final EventFilterDTO filter, final boolean includeDeleted) {
        return (root, query, cb) -> {
            final List<Predicate> predicates = new ArrayList<>();

            // Default behaviour: NEVER return soft-deleted events unless includeDeleted is true (Admin only)
            if (!includeDeleted) {
                predicates.add(cb.isFalse(root.get("deleted")));
            }

            final Join<Event, Venue> venueJoin = root.join("venue", JoinType.INNER);
            final Join<Venue, Building> buildingJoin = venueJoin.join("building", JoinType.INNER);

            if (filter != null) {
                if (filter.category() != null) {
                    predicates.add(cb.equal(root.get("category"), filter.category()));
                }

                if (filter.city() != null && !filter.city().isBlank()) {
                    predicates.add(cb.equal(
                            cb.lower(buildingJoin.get("city")),
                            filter.city().trim().toLowerCase()
                    ));
                }

                if (filter.search() != null && !filter.search().isBlank()) {
                    final String pattern = "%" + filter.search().trim().toLowerCase() + "%";
                    final Predicate titleMatch = cb.like(cb.lower(root.get("title")), pattern);
                    final Predicate venueMatch = cb.like(cb.lower(venueJoin.get("name")), pattern);
                    predicates.add(cb.or(titleMatch, venueMatch));
                }

                if (Boolean.TRUE.equals(filter.onlyAvailable())) {
                    predicates.add(cb.lessThan(root.get("soldTicketsCount"), venueJoin.get("capacity")));
                    predicates.add(cb.greaterThan(root.get("dateAndTime"), LocalDateTime.now(ZoneOffset.UTC)));
                } else if (Boolean.TRUE.equals(filter.upcomingOnly())) {
                    predicates.add(cb.greaterThan(root.get("dateAndTime"), LocalDateTime.now(ZoneOffset.UTC)));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
