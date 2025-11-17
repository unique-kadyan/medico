package com.kaddy.repository;

import com.kaddy.model.PendingUser;
import com.kaddy.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PendingUserRepository extends JpaRepository<PendingUser, Long> {

    List<PendingUser> findByStatus(String status);

    List<PendingUser> findByRequestedRole(UserRole role);

    List<PendingUser> findByStatusAndRequestedRole(String status, UserRole role);

    Optional<PendingUser> findByUsername(String username);

    Optional<PendingUser> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
