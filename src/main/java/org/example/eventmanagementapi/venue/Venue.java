package org.example.eventmanagementapi.venue;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.eventmanagementapi.building.Building;
import org.example.eventmanagementapi.common.model.BaseEntity;

@Entity
@Table(name = "venues")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class Venue extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false)
    private Integer capacity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "building_id", nullable = false)
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
