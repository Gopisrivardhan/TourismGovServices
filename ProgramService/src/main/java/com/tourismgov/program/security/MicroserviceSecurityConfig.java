package com.tourismgov.program.security;

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
                // 1. EVENT ENDPOINTS (/tourismgov/v1/events)
                // ==========================================
                // Anyone can view events and paged events
                .requestMatchers(HttpMethod.GET, "/tourismgov/v1/events/**").permitAll()
                
                // Only staff can create, update, or delete events
                .requestMatchers(HttpMethod.POST, "/tourismgov/v1/events").hasAnyRole(ADMIN, MANAGER, OFFICER)
                .requestMatchers(HttpMethod.PUT, "/tourismgov/v1/events/*").hasAnyRole(ADMIN, MANAGER, OFFICER)
                .requestMatchers(HttpMethod.PATCH, "/tourismgov/v1/events/*/status").hasAnyRole(ADMIN, MANAGER, OFFICER)
                .requestMatchers(HttpMethod.DELETE, "/tourismgov/v1/events/*").hasRole(ADMIN)

                // ==========================================
                // 2. BOOKING ENDPOINTS (/tourismgov/v1)
                // ==========================================
                // Tourists (and staff) making and managing their own bookings
                .requestMatchers(HttpMethod.POST, "/tourismgov/v1/events/*/bookings").hasAnyRole(TOURIST, OFFICER, ADMIN)
                .requestMatchers(HttpMethod.PATCH, "/tourismgov/v1/bookings/*/status").hasAnyRole(TOURIST, OFFICER, ADMIN)
                .requestMatchers(HttpMethod.GET, "/tourismgov/v1/bookings/tourist/*").hasAnyRole(TOURIST, OFFICER, ADMIN)
                
                // Staff/Auditors viewing global booking data
                .requestMatchers(HttpMethod.GET, "/tourismgov/v1/events/*/bookings/paged").hasAnyRole(ADMIN, MANAGER, OFFICER, AUDITOR, COMPLIANCE)
                .requestMatchers(HttpMethod.GET, "/tourismgov/v1/events/*/bookings").hasAnyRole(ADMIN, MANAGER, OFFICER, AUDITOR, COMPLIANCE)
                .requestMatchers(HttpMethod.GET, "/tourismgov/v1/bookings/paged").hasAnyRole(ADMIN, MANAGER, OFFICER, AUDITOR, COMPLIANCE)
                
                // Specific booking ID lookups
                .requestMatchers(HttpMethod.GET, "/tourismgov/v1/bookings/*").hasAnyRole(ADMIN, MANAGER, OFFICER, AUDITOR, COMPLIANCE, TOURIST)

                .anyRequest().authenticated()
            )
            .addFilterBefore(gatewayHeaderFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}