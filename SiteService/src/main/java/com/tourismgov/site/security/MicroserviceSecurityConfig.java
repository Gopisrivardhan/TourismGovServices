package com.tourismgov.site.security;

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

    private final GatewayHeaderFilter gatewayHeaderFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                
                // ==========================================
                // 1. HERITAGE SITE ENDPOINTS (/tourismgov/v1/sites)
                // ==========================================
                // Anyone (including unauthenticated public/tourists) can view heritage sites
                .requestMatchers(HttpMethod.GET, "/tourismgov/v1/sites", "/tourismgov/v1/sites/*").permitAll()
                
                // Only internal staff can create or update heritage sites
                .requestMatchers(HttpMethod.POST, "/tourismgov/v1/sites").hasAnyRole(ADMIN, MANAGER, OFFICER)
                .requestMatchers(HttpMethod.PUT, "/tourismgov/v1/sites/*").hasAnyRole(ADMIN, MANAGER, OFFICER)
                
                // Only Administrators can delete a heritage site
                .requestMatchers(HttpMethod.DELETE, "/tourismgov/v1/sites/*").hasRole(ADMIN)

                // ==========================================
                // 2. PRESERVATION ACTIVITY ENDPOINTS (/tourismgov/v1/preservation)
                // ==========================================
                // Staff and officers logging and updating maintenance/preservation activities
                .requestMatchers(HttpMethod.POST, "/tourismgov/v1/preservation/site/*").hasAnyRole(ADMIN, MANAGER, OFFICER)
                .requestMatchers(HttpMethod.PATCH, "/tourismgov/v1/preservation/*/status").hasAnyRole(ADMIN, MANAGER, OFFICER)
                
                // Internal staff, auditors, and compliance officers viewing preservation logs
                // Note: Tourists do not need access to internal maintenance records
                .requestMatchers(HttpMethod.GET, "/tourismgov/v1/preservation/**").hasAnyRole(ADMIN, MANAGER, OFFICER, AUDITOR, COMPLIANCE)
                
                // Only Administrators can delete a preservation log
                .requestMatchers(HttpMethod.DELETE, "/tourismgov/v1/preservation/*").hasRole(ADMIN)

                // Fallback: Any other request must be authenticated
                .anyRequest().authenticated()
            )
            .addFilterBefore(gatewayHeaderFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}