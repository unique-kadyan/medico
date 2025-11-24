package com.kaddy.controller;

import com.kaddy.dto.AuthenticationResponse;
import com.kaddy.model.User;
import com.kaddy.repository.UserRepository;
import com.kaddy.service.PermissionService;
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
public class UserController {

    private final UserRepository userRepository;
    private final PermissionService permissionService;

    @GetMapping
    public ResponseEntity<?> getAllUsers(@RequestParam(required = false) String role) {
        log.info("Fetching all users" + (role != null ? " with role: " + role : ""));

        java.util.List<User> users;
        if (role != null && !role.isEmpty()) {
            try {
                com.kaddy.model.enums.UserRole userRole = com.kaddy.model.enums.UserRole.valueOf(role.toUpperCase());
                users = userRepository.findAll().stream().filter(u -> u.getRole() == userRole)
                        .collect(java.util.stream.Collectors.toList());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Invalid role: " + role);
            }
        } else {
            users = userRepository.findAll();
        }

        java.util.List<AuthenticationResponse> responses = users.stream()
                .map(user -> AuthenticationResponse.builder().userId(user.getId()).username(user.getUsername())
                        .email(user.getEmail()).firstName(user.getFirstName()).lastName(user.getLastName())
                        .phone(user.getPhone()).role(user.getRole())
                        .permissions(permissionService.getPermissionsForRole(user.getRole())).enabled(user.getEnabled())
                        .build())
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/profile")
    public ResponseEntity<AuthenticationResponse> getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        log.info("Fetching profile for user: {}", username);

        User user = userRepository.findByUsername(username).or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        AuthenticationResponse response = AuthenticationResponse.builder().userId(user.getId())
                .username(user.getUsername()).email(user.getEmail()).firstName(user.getFirstName())
                .lastName(user.getLastName()).phone(user.getPhone()).role(user.getRole())
                .permissions(permissionService.getPermissionsForRole(user.getRole())).enabled(user.getEnabled())
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<AuthenticationResponse> getCurrentUser() {
        return getCurrentUserProfile();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AuthenticationResponse> getUserById(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        AuthenticationResponse response = AuthenticationResponse.builder().userId(user.getId())
                .username(user.getUsername()).email(user.getEmail()).firstName(user.getFirstName())
                .lastName(user.getLastName()).phone(user.getPhone()).role(user.getRole())
                .permissions(permissionService.getPermissionsForRole(user.getRole())).enabled(user.getEnabled())
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AuthenticationResponse> updateUser(@PathVariable Long id,
            @RequestBody UpdateUserRequest updateRequest) {
        log.info("Updating user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        if (updateRequest.getUsername() != null && !updateRequest.getUsername().isEmpty()) {
            user.setUsername(updateRequest.getUsername());
        }
        if (updateRequest.getEmail() != null && !updateRequest.getEmail().isEmpty()) {
            user.setEmail(updateRequest.getEmail());
        }
        if (updateRequest.getFirstName() != null && !updateRequest.getFirstName().isEmpty()) {
            user.setFirstName(updateRequest.getFirstName());
        }
        if (updateRequest.getLastName() != null && !updateRequest.getLastName().isEmpty()) {
            user.setLastName(updateRequest.getLastName());
        }
        if (updateRequest.getPhone() != null && !updateRequest.getPhone().isEmpty()) {
            user.setPhone(updateRequest.getPhone());
        }

        User updatedUser = userRepository.save(user);
        log.info("User {} updated successfully", id);

        AuthenticationResponse response = AuthenticationResponse.builder().userId(updatedUser.getId())
                .username(updatedUser.getUsername()).email(updatedUser.getEmail()).firstName(updatedUser.getFirstName())
                .lastName(updatedUser.getLastName()).phone(updatedUser.getPhone()).role(updatedUser.getRole())
                .permissions(permissionService.getPermissionsForRole(updatedUser.getRole()))
                .enabled(updatedUser.getEnabled()).build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        log.info("Deleting user with id: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        userRepository.deleteById(id);
        log.info("User {} deleted successfully", id);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/promote-to-admin")
    public ResponseEntity<AuthenticationResponse> promoteToAdmin(@PathVariable Long id) {
        log.info("Promoting user {} to Admin role", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        if (user.getRole() == com.kaddy.model.enums.UserRole.ADMIN) {
            throw new RuntimeException("User is already an Admin");
        }

        user.setRole(com.kaddy.model.enums.UserRole.ADMIN);
        User updatedUser = userRepository.save(user);
        log.info("User {} promoted to Admin successfully", id);

        AuthenticationResponse response = AuthenticationResponse.builder().userId(updatedUser.getId())
                .username(updatedUser.getUsername()).email(updatedUser.getEmail()).firstName(updatedUser.getFirstName())
                .lastName(updatedUser.getLastName()).phone(updatedUser.getPhone()).role(updatedUser.getRole())
                .permissions(permissionService.getPermissionsForRole(updatedUser.getRole()))
                .enabled(updatedUser.getEnabled()).build();

        return ResponseEntity.ok(response);
    }

    @lombok.Data
    public static class UpdateUserRequest {
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String phone;
    }
}
