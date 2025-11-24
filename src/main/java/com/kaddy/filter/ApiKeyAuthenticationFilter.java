package com.kaddy.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
@Order(1)
@RequiredArgsConstructor
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    @Value("${app.api.key:your-secret-api-key-change-in-production}")
    private String apiKey;

    @Value("${app.api.enabled:true}")
    private boolean apiKeyEnabled;

    private static final List<String> EXCLUDED_PATHS = Arrays.asList("/api/auth/login", "/api/auth/register",
            "/v3/api-docs", "/actuator");

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (shouldSkipFilter(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!apiKeyEnabled) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestApiKey = request.getHeader("X-API-Key");

        if (requestApiKey == null || requestApiKey.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter()
                    .write("{\"error\": \"Missing API Key\", \"message\": \"X-API-Key header is required\"}");
            return;
        }

        if (!apiKey.equals(requestApiKey)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter()
                    .write("{\"error\": \"Invalid API Key\", \"message\": \"The provided API key is invalid\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean shouldSkipFilter(String path) {
        return EXCLUDED_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }
}
