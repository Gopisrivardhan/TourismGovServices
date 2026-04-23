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
    private final GatewayHeaderFilter gatewayHeaderFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                
                // ==========================================
                // 1. TOURISM PROGRAM ENDPOINTS (/tourismgov/v1/programs)
                // ==========================================
                // Internal Reporting: Budget reports are strictly for higher-level internal staff and auditors
                .requestMatchers(HttpMethod.GET, "/tourismgov/v1/programs/*/budget-report").hasAnyRole(ADMIN, MANAGER, AUDITOR, COMPLIANCE)

                // Public Access: Anyone can view the catalog of active tourism programs (Tourists, Unauthenticated users)
                .requestMatchers(HttpMethod.GET, "/tourismgov/v1/programs", "/tourismgov/v1/programs/*", "/tourismgov/v1/programs/paged").permitAll()
                
                // Program Management: Only Admins and Program Managers can create or fully update a program
                .requestMatchers(HttpMethod.POST, "/tourismgov/v1/programs").hasAnyRole(ADMIN, MANAGER)
                .requestMatchers(HttpMethod.PUT, "/tourismgov/v1/programs/*").hasAnyRole(ADMIN, MANAGER)
                .requestMatchers(HttpMethod.PATCH, "/tourismgov/v1/programs/*/status").hasAnyRole(ADMIN, MANAGER)
                
                // Only Administrators can delete a whole tourism program
                .requestMatchers(HttpMethod.DELETE, "/tourismgov/v1/programs/*").hasRole(ADMIN)

                // ==========================================
                // 2. RESOURCE ALLOCATION ENDPOINTS (/tourismgov/v1/resources)
                // ==========================================
                // Resource Management: Admins and Program Managers allocate and update resources (Funds, Staff, Venues)
                .requestMatchers(HttpMethod.POST, "/tourismgov/v1/resources/program/*").hasAnyRole(ADMIN, MANAGER)
                .requestMatchers(HttpMethod.PATCH, "/tourismgov/v1/resources/*/status").hasAnyRole(ADMIN, MANAGER)
                
                // Viewing Resources: Internal staff and auditors can view allocated resources (Tourists do not need to see this)
                .requestMatchers(HttpMethod.GET, "/tourismgov/v1/resources/program/*").hasAnyRole(ADMIN, MANAGER, OFFICER, AUDITOR, COMPLIANCE)
                
                // Only Administrators can delete a resource record entirely
                .requestMatchers(HttpMethod.DELETE, "/tourismgov/v1/resources/*").hasRole(ADMIN)

                // Fallback: Any other request must be authenticated
                .anyRequest().authenticated()
            )
            .addFilterBefore(gatewayHeaderFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}