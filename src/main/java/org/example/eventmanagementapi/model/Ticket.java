package org.example.eventmanagementapi.model;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;
import java.math.BigDecimal;

import java.util.UUID;

@Entity
@Table(
        name = "tickets",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_event_seat",
                columnNames = {"event_id", "seat_number"}
        )
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class Ticket extends BaseEntity  {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(name = "seat_number", nullable = false, length = 50)
    @Setter(AccessLevel.NONE)
    private String seatNumber;

    @Column(name = "price_paid", nullable = false, updatable = false, precision = 10, scale = 2)
    @Setter(AccessLevel.NONE)
    private BigDecimal pricePaid;

    public Ticket(Customer customer, Event event, String seatNumber) {
        this.customer = customer;
        this.event = event;
        this.seatNumber = seatNumber;
        this.pricePaid = event.getBasePrice();
    }
}
