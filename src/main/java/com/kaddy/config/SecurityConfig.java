package com.kaddy.config;

import com.kaddy.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    @Profile("prod")
    public SecurityFilterChain productionFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/api/auth/**"))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()

                        .requestMatchers("/api/patients/**").hasAnyRole("ADMIN", "DOCTOR", "NURSE", "RECEPTIONIST")
                        .requestMatchers("/api/async/patients/**")
                        .hasAnyRole("ADMIN", "DOCTOR", "NURSE", "RECEPTIONIST")

                        .requestMatchers("/api/medications/**").hasAnyRole("ADMIN", "PHARMACIST", "DOCTOR")
                        .requestMatchers("/api/async/medications/**").hasAnyRole("ADMIN", "PHARMACIST", "DOCTOR")

                        .requestMatchers("/api/doctors/**").hasAnyRole("ADMIN", "RECEPTIONIST", "DOCTOR", "NURSE")

                        .requestMatchers("/api/assignments").hasRole("ADMIN")
                        .requestMatchers("/api/assignments/{id}").hasRole("ADMIN")
                        .requestMatchers("/api/assignments/doctor/**").hasAnyRole("ADMIN", "DOCTOR", "NURSE")
                        .requestMatchers("/api/assignments/patient/**").hasAnyRole("ADMIN", "DOCTOR", "NURSE")

                        .requestMatchers("/api/nurse-assignments/**").hasAnyRole("ADMIN", "NURSE")

                        .requestMatchers("/api/appointments/**").hasAnyRole("ADMIN", "DOCTOR", "NURSE", "RECEPTIONIST")

                        .requestMatchers("/api/medication-requests/approve/**").hasRole("ADMIN")
                        .requestMatchers("/api/medication-requests/**").hasAnyRole("ADMIN", "DOCTOR", "PHARMACIST")

                        .requestMatchers("/api/followups/**").hasAnyRole("ADMIN", "DOCTOR", "NURSE")

                        .requestMatchers("/api/lab-tests/*/results").hasAnyRole("ADMIN", "LAB_TECHNICIAN")
                        .requestMatchers("/api/lab-tests/**").hasAnyRole("ADMIN", "DOCTOR", "NURSE", "LAB_TECHNICIAN", "PATIENT")

                        .requestMatchers("/api/files/**").hasAnyRole("ADMIN", "DOCTOR", "NURSE", "LAB_TECHNICIAN", "PHARMACIST", "RECEPTIONIST")

                        .requestMatchers("/api/notifications/**").authenticated()

                        .requestMatchers("/api/monitoring/**").hasRole("ADMIN")

                        .requestMatchers("/api/async/*/batch/**").hasRole("ADMIN")

                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives(
                                        "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; font-src 'self'"))
                        .xssProtection(xss -> xss
                                .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                        .contentTypeOptions(contentType -> {
                        })
                        .frameOptions(frame -> frame.deny())
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)));

        return http.build();
    }

    @Bean
    @Profile({ "dev", "default" })
    public SecurityFilterChain developmentFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()

                        .requestMatchers("/api/pending-users/**").hasAnyRole("ADMIN", "DOCTOR_SUPERVISOR", "NURSE_MANAGER", "NURSE_SUPERVISOR")

                        .requestMatchers("/api/users/profile", "/api/users/me").authenticated()
                        .requestMatchers("/api/users").hasRole("ADMIN")
                        .requestMatchers("/api/users/{id}").hasRole("ADMIN")

                        .requestMatchers("/api/patients").hasAnyRole("ADMIN", "DOCTOR", "NURSE", "RECEPTIONIST")
                        .requestMatchers("/api/patients/active").hasAnyRole("ADMIN", "DOCTOR", "NURSE", "RECEPTIONIST")
                        .requestMatchers("/api/patients/search").hasAnyRole("ADMIN", "DOCTOR", "NURSE", "RECEPTIONIST")
                        .requestMatchers("/api/patients/{id}").hasAnyRole("ADMIN", "DOCTOR", "NURSE", "RECEPTIONIST")
                        .requestMatchers("/api/patients/patient-id/{patientId}").hasAnyRole("ADMIN", "DOCTOR", "NURSE", "RECEPTIONIST")
                        .requestMatchers("/api/async/patients/**").hasAnyRole("ADMIN", "DOCTOR", "NURSE", "RECEPTIONIST")

                        .requestMatchers("/api/patient-portal/**").hasRole("PATIENT")

                        .requestMatchers("/api/doctors").hasAnyRole("ADMIN", "RECEPTIONIST", "DOCTOR", "NURSE")
                        .requestMatchers("/api/doctors/{id}").hasAnyRole("ADMIN", "RECEPTIONIST", "DOCTOR", "NURSE")
                        .requestMatchers("/api/doctors/**").hasAnyRole("ADMIN", "RECEPTIONIST", "DOCTOR", "NURSE")

                        .requestMatchers("/api/assignments").hasRole("ADMIN")
                        .requestMatchers("/api/assignments/{id}").hasRole("ADMIN")
                        .requestMatchers("/api/assignments/doctor/**").hasAnyRole("ADMIN", "DOCTOR", "NURSE")
                        .requestMatchers("/api/assignments/patient/**").hasAnyRole("ADMIN", "DOCTOR", "NURSE")

                        .requestMatchers("/api/nurse-assignments/**").hasAnyRole("ADMIN", "NURSE")

                        .requestMatchers("/api/appointments/**").hasAnyRole("ADMIN", "DOCTOR", "NURSE", "RECEPTIONIST", "PATIENT")

                        .requestMatchers("/api/medications/**").hasAnyRole("ADMIN", "PHARMACIST", "DOCTOR")
                        .requestMatchers("/api/async/medications/**").hasAnyRole("ADMIN", "PHARMACIST", "DOCTOR")

                        .requestMatchers("/api/medication-requests/approve/**").hasRole("ADMIN")
                        .requestMatchers("/api/medication-requests/**").hasAnyRole("ADMIN", "DOCTOR", "PHARMACIST")

                        .requestMatchers("/api/followups/**").hasAnyRole("ADMIN", "DOCTOR", "NURSE")

                        .requestMatchers("/api/lab-tests/*/results").hasAnyRole("ADMIN", "LAB_TECHNICIAN")
                        .requestMatchers("/api/lab-tests/**").hasAnyRole("ADMIN", "DOCTOR", "NURSE", "LAB_TECHNICIAN", "PATIENT")

                        .requestMatchers("/api/files/**").hasAnyRole("ADMIN", "DOCTOR", "NURSE", "LAB_TECHNICIAN", "PHARMACIST", "RECEPTIONIST")

                        .requestMatchers("/api/notifications/**").authenticated()

                        .requestMatchers("/api/monitoring/**").hasRole("ADMIN")

                        .requestMatchers("/api/async/*/batch/**").hasRole("ADMIN")

                        .anyRequest().authenticated())
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin()));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
