package org.example.eventmanagementapi.ticket;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.eventmanagementapi.common.model.BaseEntity;
import org.example.eventmanagementapi.user.User;
import org.example.eventmanagementapi.event.Event;

import java.math.BigDecimal;

@Entity
@Table(
        name = "ticket",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_event_seat",
                columnNames = {"event_id", "seat_number"}
        )
)
@NoArgsConstructor
@Getter
@Setter
public class Ticket extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "seat_number", nullable = false, length = 50)
    private String seatNumber;

    @Column(name = "price_paid", nullable = false, updatable = false, precision = 10, scale = 2)
    private BigDecimal pricePaid;

    public Ticket(User user, Event event, String seatNumber) {
        this.user = user;
        this.event = event;
        this.seatNumber = seatNumber;
        this.pricePaid = event.getBasePrice();
    }
}
