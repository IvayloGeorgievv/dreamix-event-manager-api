package org.example.eventmanagementapi.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("""
            SELECT u
            FROM User u
            WHERE (:includeDeleted = true OR u.deleted = false)
            AND (
                :search IS NULL OR :search = ''
                OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%'))
                OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))
            )
            """)
    Page<User> findUsers(
            @Param("search") String search,
            @Param("includeDeleted") boolean includeDeleted,
            Pageable pageable
    );

    Optional<User> findByIdAndDeletedFalse(UUID id);

    Optional<User> findByEmailAndDeletedFalse(String email);

    boolean existsByEmailAndDeletedFalse(String email);
}
