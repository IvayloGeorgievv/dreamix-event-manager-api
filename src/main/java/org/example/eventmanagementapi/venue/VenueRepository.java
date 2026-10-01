package org.example.eventmanagementapi.venue;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VenueRepository extends JpaRepository<Venue, UUID> {

    @EntityGraph(attributePaths = {"building"})
    @Query("""
            SELECT v
            FROM Venue v
            JOIN v.building b
            WHERE (:includeDeleted = true OR v.deleted = false)
            AND (
                :search IS NULL OR :search = ''
                OR LOWER(v.name) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(b.city) LIKE LOWER(CONCAT('%', :search, '%'))
            )
            """)
    Page<Venue> findVenues(
            @Param("search") String search,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"building"})
    Optional<Venue> findByIdAndDeletedFalse(UUID id);

    boolean existsByIdAndDeletedFalse(UUID id);
}
