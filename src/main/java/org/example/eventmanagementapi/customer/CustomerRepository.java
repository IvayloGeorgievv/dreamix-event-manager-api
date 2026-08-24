package org.example.eventmanagementapi.customer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    List<Customer> findAllByDeletedFalse();

    Optional<Customer> findByIdAndDeletedFalse(UUID id);

    Optional<Customer> findByEmailAndDeletedFalse(String email);

    boolean existsByIdAndDeletedFalse(UUID id);

    boolean existsByEmailAndDeletedFalse(String email);
}
