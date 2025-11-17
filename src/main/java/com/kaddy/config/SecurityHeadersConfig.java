package com.kaddy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
public class SecurityHeadersConfig {

    @Bean
    public OncePerRequestFilter securityHeadersFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                    FilterChain filterChain)
                    throws ServletException, IOException {

                response.setHeader("X-Frame-Options", "DENY");

                response.setHeader("X-Content-Type-Options", "nosniff");

                response.setHeader("X-XSS-Protection", "1; mode=block");

                response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

                response.setHeader("Content-Security-Policy",
                        "default-src 'self'; " +
                                "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                                "style-src 'self' 'unsafe-inline'; " +
                                "img-src 'self' data: https:; " +
                                "font-src 'self' data:; " +
                                "connect-src 'self'; " +
                                "frame-ancestors 'none'");

                response.setHeader("Permissions-Policy",
                        "geolocation=(), " +
                                "microphone=(), " +
                                "camera=(), " +
                                "payment=(), " +
                                "usb=()");

                if (request.isSecure()) {
                    response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
                }

                filterChain.doFilter(request, response);
            }
        };
    }
}
