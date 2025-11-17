package com.kaddy.service;

import com.kaddy.dto.AuthenticationRequest;
import com.kaddy.dto.AuthenticationResponse;
import com.kaddy.dto.RegisterRequest;
import com.kaddy.model.User;
import com.kaddy.repository.UserRepository;
import com.kaddy.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final PermissionService permissionService;

    public AuthenticationResponse register(RegisterRequest request) {
        log.info("Registering new user: {}", request.getUsername());

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());
        user.setEnabled(true);

        User savedUser = userRepository.save(user);
        log.info("User registered successfully: {}", savedUser.getUsername());

        UserDetails userDetails = userDetailsService.loadUserByUsername(savedUser.getUsername());
        String jwtToken = jwtService.generateToken(userDetails);

        return buildAuthenticationResponse(savedUser, jwtToken);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        log.info("Authenticating user with email: {}", request.getEmail());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (Exception e) {
            log.error("Authentication failed for user: {}", request.getEmail(), e);
            throw new RuntimeException("Invalid credentials. Please check your email/username and password.");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .or(() -> userRepository.findByUsername(request.getEmail()))
                .orElseThrow(() -> {
                    log.error("User not found after successful authentication: {}", request.getEmail());
                    return new RuntimeException("User not found");
                });

        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String jwtToken = jwtService.generateToken(userDetails);

        log.info("User authenticated successfully: {}", user.getUsername());

        return buildAuthenticationResponse(user, jwtToken);
    }

    private AuthenticationResponse buildAuthenticationResponse(User user, String token) {
        return AuthenticationResponse.builder()
                .token(token)
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .userId(user.getId())
                .permissions(permissionService.getPermissionsForRole(user.getRole()))
                .build();
    }
}
