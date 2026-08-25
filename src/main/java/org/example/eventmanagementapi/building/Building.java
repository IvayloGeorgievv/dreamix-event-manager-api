package org.example.eventmanagementapi.building;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.eventmanagementapi.common.model.BaseEntity;

@Entity
@Table(name = "building")
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

    public Building(String name, String city, String address) {
        this.name = name;
        this.city = city;
        this.address = address;
    }
}
