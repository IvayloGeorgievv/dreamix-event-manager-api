package org.example.eventmanagementapi.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "venues")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class Venue extends BaseEntity  {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer capacity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
    @Setter(AccessLevel.PACKAGE)
    private Building building;

    public Venue(String name, Integer capacity, Building building) {
        this.name = name;
        this.capacity = capacity;
        this.building = building;
    }

    public String getCity() {
        return building != null ? building.getCity() : null;
    }

    public String getAddress() {
        return building != null ? building.getAddress() : null;
    }
}
