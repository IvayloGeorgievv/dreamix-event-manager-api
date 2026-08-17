package org.example.eventmanagementapi.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "performers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class Performer extends BaseEntity  {

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToMany(mappedBy = "performers")
    @Setter(AccessLevel.NONE)
    private final List<Event> events = new ArrayList<>();

    public Performer(String name) {
        this.name = name;
    }

    public List<Event> getEvents() {
        return Collections.unmodifiableList(events);
    }

    //package-private helper methods: To add/remove the Event inside PerformerRepository's Set of Events
    void internalAddEvent(Event event) {
        events.add(event);
    }

    void internalRemoveEvent(Event event) {
        this.events.remove(event);
    }

}
