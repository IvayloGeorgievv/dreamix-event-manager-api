package org.example.eventmanagementapi.performer;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PerformerRepository extends JpaRepository<Performer, UUID> {

    @Query("""
            SELECT p
            FROM Performer p
            WHERE (:includeDeleted = true OR p.deleted = false)
            AND (
                :search IS NULL OR :search = ''
                OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
            )
            """)
    Page<Performer> findPerformers(
            @Param("search") String search,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable
    );

    Optional<Performer> findByIdAndDeletedFalse(UUID id);

    boolean existsByIdAndDeletedFalse(UUID id);
}
