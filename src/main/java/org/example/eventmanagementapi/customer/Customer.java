package org.example.eventmanagementapi.customer;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.eventmanagementapi.common.model.BaseEntity;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "customers")
@SecondaryTable(
        name = "customer_profiles",
        pkJoinColumns = @PrimaryKeyJoinColumn(name = "customer_id")
)
@NoArgsConstructor
@Getter
@Setter
@NullMarked
public class Customer extends BaseEntity implements UserDetails {

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 150)
    private String lastName;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Role role;

    @Column(nullable = false)
    private String password;

    @Nullable
    @Column(name = "refresh_token", length = 512)
    private String refreshToken;

    @Nullable
    @Column(table = "customer_profiles", name = "phone_number", length = 30)
    private String phoneNumber;

    @Nullable
    @Column(table = "customer_profiles", name = "address", length = 200)
    private String address;

    @Nullable
    @Column(table = "customer_profiles", name = "postal_code", length = 20)
    private String postalCode;

    public Customer(String firstName, String lastName, String email, String password, Role role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    // Returns list of assigned roles for Spring Security permission checks
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    // Returns the Customer's email to be used as the unique username for Spring Security authentication
    @Override
    public String getUsername(){
        return this.email;
    }

    // Determines if Customer is active based on soft-delete status
    @Override
    public boolean isEnabled() {
        return !isDeleted();
    }
}
