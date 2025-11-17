package com.kaddy.repository;

import com.kaddy.model.User;
import com.kaddy.model.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findByRole(UserRole role);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
}
