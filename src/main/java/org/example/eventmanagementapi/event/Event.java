package org.example.eventmanagementapi.event;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.eventmanagementapi.common.model.BaseEntity;
import org.example.eventmanagementapi.performer.Performer;
import org.example.eventmanagementapi.venue.Venue;

import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class Event extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "sold_tickets_count", nullable = false)
    @Setter(AccessLevel.NONE)
    private Integer soldTicketsCount = 0;

    @Column(name = "date_and_time", nullable = false)
    private LocalDateTime dateAndTime;

    @ManyToMany
    @JoinTable(
            name = "event_performers",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "performer_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_event_performer",
                    columnNames = {"event_id", "performer_id"}
            )
    )
    @Setter(AccessLevel.NONE)
    private final List<Performer> performers = new ArrayList<>();

    public Event(Venue venue, String title, BigDecimal basePrice, LocalDateTime dateAndTime) {
        this.venue = venue;
        this.title = title;
        this.basePrice = basePrice;
        this.dateAndTime = dateAndTime;
    }

    public void incrementSoldTickets() {
        this.soldTicketsCount++;
    }

    public void decrementSoldTickets() {
        this.soldTicketsCount--;
    }

    public boolean addPerformer(Performer performer) {
        return performers.add(performer);
    }

    public boolean removePerformer(Performer performer) {
        return performers.remove(performer);
    }

    public List<Performer> getPerformers() {
        return Collections.unmodifiableList(performers);
    }
}
