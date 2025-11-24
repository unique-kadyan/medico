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

    public Optional<UserRole> getCurrentUserRole() {
        return getCurrentUser().map(User::getRole);
    }

    public boolean hasRole(UserRole role) {
        return getCurrentUserRole().map(currentRole -> currentRole.equals(role)).orElse(false);
    }

    public boolean isAdmin() {
        return hasRole(UserRole.ADMIN);
    }

    public boolean isDoctor() {
        return hasRole(UserRole.DOCTOR);
    }

    public boolean isNurse() {
        return hasRole(UserRole.NURSE);
    }

    public boolean isPharmacist() {
        return hasRole(UserRole.PHARMACIST);
    }

    public Optional<Long> getCurrentUserId() {
        return getCurrentUser().map(User::getId);
    }

    public Optional<Long> getCurrentDoctorId() {
        return getCurrentUser().filter(user -> user.getRole() == UserRole.DOCTOR).map(User::getId); // Placeholder -
                                                                                                    // adjust based on
                                                                                                    // your User-Doctor
                                                                                                    // relationship
    }
}
