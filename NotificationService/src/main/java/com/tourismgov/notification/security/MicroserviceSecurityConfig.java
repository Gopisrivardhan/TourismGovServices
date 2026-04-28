package com.tourismgov.notification.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class MicroserviceSecurityConfig {

    private static final String ADMIN = "ADMIN";
    private static final String OFFICER = "OFFICER";
    private static final String MANAGER = "MANAGER";
    private static final String AUDITOR = "AUDITOR";
    private static final String COMPLIANCE = "COMPLIANCE";
    private static final String TOURIST = "TOURIST";

    private final GatewayHeaderFilter gatewayHeaderFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth

                // ==========================================
                // INTERNAL: Service-to-Service endpoints (no auth required)
                // ==========================================
                .requestMatchers(HttpMethod.POST, "/tourismgov/v1/notifications/system-alert").permitAll()
                .requestMatchers(HttpMethod.GET,  "/tourismgov/v1/notifications/internal/**").permitAll()
                // Broadcast is open to internal Feign calls too — role check is in the service layer
                .requestMatchers(HttpMethod.POST, "/tourismgov/v1/notifications/broadcast").permitAll()

                // ==========================================
                // NOTIFICATION ENDPOINTS — require authenticated user (X-User-Id from Gateway)
                // ==========================================
                .requestMatchers(HttpMethod.POST, "/tourismgov/v1/notifications").authenticated()
                .requestMatchers(HttpMethod.GET,  "/tourismgov/v1/notifications").authenticated()
                .requestMatchers(HttpMethod.GET,  "/tourismgov/v1/notifications/unread").authenticated()
                .requestMatchers(HttpMethod.GET,  "/tourismgov/v1/notifications/category/**").authenticated()
                .requestMatchers(HttpMethod.PATCH,"/tourismgov/v1/notifications/*/read").authenticated()
                .requestMatchers(HttpMethod.PATCH,"/tourismgov/v1/notifications/read-all").authenticated()

                .anyRequest().authenticated()
            )
            .addFilterBefore(gatewayHeaderFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}