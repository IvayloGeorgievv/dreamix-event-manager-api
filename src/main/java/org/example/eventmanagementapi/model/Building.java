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
@Table(name = "buildings")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class Building extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String city;

    @Column(nullable = false, length = 200)
    private String address;

    @OneToMany(mappedBy = "building", orphanRemoval = true)
    @Setter(AccessLevel.NONE)
    private final List<Venue> venues = new ArrayList<>();


    public Building(String name, String city, String address) {
        this.name = name;
        this.city = city;
        this.address = address;
    }

    public List<Venue> getVenues() {
        return Collections.unmodifiableList(venues);
    }

    // Helper methods
    public void addVenue(Venue venue) {
        venues.add(venue);
        venue.setBuilding(this);
    }

    public void removeVenue(Venue venue) {
        venues.remove(venue);
        venue.setBuilding(null);
    }

    public int getVenueCount() {
        return venues.size();
    }
}
