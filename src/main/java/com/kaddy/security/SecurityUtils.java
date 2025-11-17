package com.kaddy.security;

import com.kaddy.model.User;
import com.kaddy.model.enums.UserRole;
import com.kaddy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    /**
     * Get the currently authenticated user
     */
    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByUsername(username);
        } else if (principal instanceof String) {
            return userRepository.findByUsername((String) principal);
        }

        return Optional.empty();
    }

    /**
     * Get the current user's role
     */
    public Optional<UserRole> getCurrentUserRole() {
        return getCurrentUser().map(User::getRole);
    }

    /**
     * Check if current user has a specific role
     */
    public boolean hasRole(UserRole role) {
        return getCurrentUserRole()
                .map(currentRole -> currentRole.equals(role))
                .orElse(false);
    }

    /**
     * Check if current user is an admin
     */
    public boolean isAdmin() {
        return hasRole(UserRole.ADMIN);
    }

    /**
     * Check if current user is a doctor
     */
    public boolean isDoctor() {
        return hasRole(UserRole.DOCTOR);
    }

    /**
     * Check if current user is a nurse
     */
    public boolean isNurse() {
        return hasRole(UserRole.NURSE);
    }

    /**
     * Check if current user is a pharmacist
     */
    public boolean isPharmacist() {
        return hasRole(UserRole.PHARMACIST);
    }

    /**
     * Get current user ID
     */
    public Optional<Long> getCurrentUserId() {
        return getCurrentUser().map(User::getId);
    }

    /**
     * Get associated doctor ID for current user (if user is a doctor)
     */
    public Optional<Long> getCurrentDoctorId() {
        // This assumes you have a way to link User to Doctor entity
        // You may need to add a doctorId field to User entity or maintain a separate mapping
        return getCurrentUser()
                .filter(user -> user.getRole() == UserRole.DOCTOR)
                .map(User::getId); // Placeholder - adjust based on your User-Doctor relationship
    }
}
