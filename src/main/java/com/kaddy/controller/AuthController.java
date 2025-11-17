package com.kaddy.controller;

import com.kaddy.dto.AuthenticationRequest;
import com.kaddy.dto.AuthenticationResponse;
import com.kaddy.dto.RegisterRequest;
import com.kaddy.model.PendingUser;
import com.kaddy.model.enums.UserRole;
import com.kaddy.service.AuthenticationService;
import com.kaddy.service.PendingUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication management APIs")
public class AuthController {

    private final AuthenticationService authenticationService;
    private final PendingUserService pendingUserService;

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT token")
    public ResponseEntity<AuthenticationResponse> login(
            @Valid @RequestBody AuthenticationRequest request) {
        log.info("Login request received for email: {}", request.getEmail());
        return ResponseEntity.ok(authenticationService.authenticate(request));
    }

    @PostMapping("/register")
    @Operation(summary = "User registration", description = "Register a new user")
    public ResponseEntity<AuthenticationResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for username: {}", request.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(authenticationService.register(request));
    }

    @PostMapping(value = "/register-with-documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Register with documents", description = "Register a new user with government ID documents (requires approval)")
    public ResponseEntity<?> registerWithDocuments(
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("email") String email,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam("role") UserRole role,
            @RequestParam("documents") List<MultipartFile> documents) {

        try {
            log.info("Registration with documents request received for username: {} with role: {}", username, role);

            if (documents == null || documents.size() < 2) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "At least 2 government ID documents are required"));
            }

            if (role == UserRole.PATIENT) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Patient accounts can only be created by receptionists"));
            }

            PendingUser pendingUser = new PendingUser();
            pendingUser.setUsername(username);
            pendingUser.setPassword(password);
            pendingUser.setEmail(email);
            pendingUser.setFirstName(firstName);
            pendingUser.setLastName(lastName);
            pendingUser.setPhone(phone);
            pendingUser.setRequestedRole(role);

            PendingUser savedPendingUser = pendingUserService.createPendingUser(pendingUser, documents);

            log.info("Pending registration created successfully for username: {}", username);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of(
                            "message", "Registration submitted successfully. Your account is pending approval.",
                            "pendingUserId", savedPendingUser.getId(),
                            "status", "PENDING"));
        } catch (Exception e) {
            log.error("Error during registration with documents for username: {}", username, e);
            return ResponseEntity.badRequest()
                    .body(Map.of("message", e.getMessage()));
        }
    }
}
