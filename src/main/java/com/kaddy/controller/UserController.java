package com.kaddy.controller;

import com.kaddy.dto.AuthenticationResponse;
import com.kaddy.model.User;
import com.kaddy.repository.UserRepository;
import com.kaddy.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "User Management", description = "APIs for user profile and account management")
public class UserController {

    private final UserRepository userRepository;
    private final PermissionService permissionService;

    @GetMapping("/profile")
    @Operation(summary = "Get current user profile", description = "Get the profile of the currently logged-in user")
    public ResponseEntity<AuthenticationResponse> getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        log.info("Fetching profile for user: {}", username);

        User user = userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        AuthenticationResponse response = AuthenticationResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .permissions(permissionService.getPermissionsForRole(user.getRole()))
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user info", description = "Alternative endpoint to get current user information")
    public ResponseEntity<AuthenticationResponse> getCurrentUser() {
        return getCurrentUserProfile();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Get user information by ID (Admin only)")
    public ResponseEntity<AuthenticationResponse> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        AuthenticationResponse response = AuthenticationResponse.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .permissions(permissionService.getPermissionsForRole(user.getRole()))
                .build();

        return ResponseEntity.ok(response);
    }
}
