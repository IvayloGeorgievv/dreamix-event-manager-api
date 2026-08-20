package org.example.eventmanagementapi.building;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
interface BuildingRepository extends JpaRepository<Building, UUID> {

    List<Building> findAllByDeletedFalse();

    Optional<Building> findByIdAndDeletedFalse(UUID id);

    boolean existsByIdAndDeletedFalse(UUID id);
}
