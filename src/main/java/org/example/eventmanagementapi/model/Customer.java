package org.example.eventmanagementapi.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "customers")
@SecondaryTable(
        name = "customer_profiles",
        pkJoinColumns = @PrimaryKeyJoinColumn(name = "customer_id")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
public class Customer extends BaseEntity {

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 150)
    private String lastName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(table = "customer_profiles", name = "phone_number", length = 30)
    private String phoneNumber;

    @Column(table = "customer_profiles", name = "address", length = 200)
    private String address;

    @Column(table = "customer_profiles", name = "postal_code", length = 20)
    private String postalCode;

    public Customer(String firstName, String lastName, String email) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }

    public Customer(String firstName, String lastName, String email, String phoneNumber, String address, String postalCode) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.postalCode = postalCode;
    }
}
