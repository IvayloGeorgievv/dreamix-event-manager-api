package org.example.eventmanagementapi.performer;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.eventmanagementapi.common.model.BaseEntity;

@Entity
@Table(name = "performers")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class Performer extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    public Performer(String name) {
        this.name = name;
    }

}
