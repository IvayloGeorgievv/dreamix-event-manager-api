package org.example.eventmanagementapi.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    List<User> findAllByDeletedFalse();

    Optional<User> findByIdAndDeletedFalse(UUID id);

    Optional<User> findByEmailAndDeletedFalse(String email);

    Optional<User> findByRefreshTokenAndDeletedFalse(String refreshToken);

    boolean existsByIdAndDeletedFalse(UUID id);

    boolean existsByEmailAndDeletedFalse(String email);
}
