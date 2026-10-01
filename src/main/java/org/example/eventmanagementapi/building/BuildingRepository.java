package org.example.eventmanagementapi.building;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BuildingRepository extends JpaRepository<Building, UUID> {

    @Query("""
            SELECT b
            FROM Building b
            WHERE (:includeDeleted = true OR b.deleted = false)
            AND (
                :search IS NULL OR :search = ''
                OR LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(b.city) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(b.address) LIKE LOWER(CONCAT('%', :search, '%'))
            )
            """)
    Page<Building> findBuildings(
            @Param("search") String search,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable
    );

    Optional<Building> findByIdAndDeletedFalse(UUID id);

    boolean existsByIdAndDeletedFalse(UUID id);
}
