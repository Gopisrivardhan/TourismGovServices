package com.tourismgov.report.security;

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
    // Tourists are intentionally omitted below to block them from viewing reports

    private final GatewayHeaderFilter gatewayHeaderFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                
                // 1. DASHBOARD ENDPOINTS
                .requestMatchers(HttpMethod.GET, "/tourismgov/v1/dashboard/stats")
                    .hasAnyRole(ADMIN, MANAGER, OFFICER, AUDITOR, COMPLIANCE)
                
                // 2. REPORT ENDPOINTS
                .requestMatchers(HttpMethod.POST, "/tourismgov/v1/reports/generate")
                    .hasAnyRole(ADMIN, MANAGER, OFFICER, AUDITOR, COMPLIANCE)
                .requestMatchers(HttpMethod.GET, "/tourismgov/v1/reports/history")
                    .hasAnyRole(ADMIN, MANAGER, OFFICER, AUDITOR, COMPLIANCE)
                .requestMatchers(HttpMethod.GET, "/tourismgov/v1/reports/download/*")
                    .hasAnyRole(ADMIN, MANAGER, OFFICER, AUDITOR, COMPLIANCE)

                .anyRequest().authenticated()
            )
            .addFilterBefore(gatewayHeaderFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}