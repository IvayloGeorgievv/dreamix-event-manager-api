package org.example.eventmanagementapi.repository;

import org.example.eventmanagementapi.model.Performer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PerformerRepository extends JpaRepository<Performer, UUID> {

    List<Performer> findAllByDeletedFalse();

    Optional<Performer> findByIdAndDeletedFalse(UUID id);

    boolean existsByIdAndDeletedFalse(UUID id);
}
