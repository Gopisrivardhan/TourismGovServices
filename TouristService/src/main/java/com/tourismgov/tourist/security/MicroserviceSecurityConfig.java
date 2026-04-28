package com.tourismgov.tourist.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.RequiredArgsConstructor;

import org.springframework.core.annotation.Order;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class MicroserviceSecurityConfig {

    private static final String ADMIN = "ADMIN";
    private static final String OFFICER = "OFFICER";
    private static final String MANAGER = "MANAGER";
    private static final String AUDITOR = "AUDITOR";
    private static final String TOURIST = "TOURIST";

    private final GatewayHeaderFilter gatewayHeaderFilter;

    @Bean
    @Order(1)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                
                // ==========================================
                // 1. TOURIST PROFILE ENDPOINTS (/tourismgov/v1/tourist)
                // ==========================================
                // Public Access: Allow anyone to create a new tourist profile
                .requestMatchers(HttpMethod.POST, "/tourismgov/v1/tourist/create").permitAll()
                // Internal: Service-to-service sync endpoint (no auth required)
                .requestMatchers(HttpMethod.POST, "/tourismgov/v1/tourist/internal/**").permitAll()

                // Tourist/Admin Access: A tourist can view/update their own profile, Admins can manage them
                .requestMatchers(HttpMethod.GET, "/tourismgov/v1/tourist/*").hasAnyRole(TOURIST, ADMIN, OFFICER, MANAGER)
                .requestMatchers(HttpMethod.PUT, "/tourismgov/v1/tourist/*/update").hasAnyRole(TOURIST, ADMIN, MANAGER)

                // Administrative: Only Admins can view the full list of profiles or delete a tourist
                .requestMatchers(HttpMethod.GET, "/tourismgov/v1/tourist/admin").hasAnyRole(ADMIN, MANAGER, OFFICER, AUDITOR)
                .requestMatchers(HttpMethod.DELETE, "/tourismgov/v1/tourist/*").hasRole(ADMIN)

                // ==========================================
                // 2. TOURIST DOCUMENT ENDPOINTS (/tourismgov/v1/touristdoc)
                // ==========================================
                // Tourist Access: Tourists can upload, view, or delete their own documents
                .requestMatchers(HttpMethod.POST, "/tourismgov/v1/touristdoc/*/documents").hasAnyRole(TOURIST, ADMIN)
                .requestMatchers(HttpMethod.GET, "/tourismgov/v1/touristdoc/*/documents/*/view").hasAnyRole(TOURIST, OFFICER, ADMIN, MANAGER)
                .requestMatchers(HttpMethod.DELETE, "/tourismgov/v1/touristdoc/*/documents/*").hasAnyRole(TOURIST, ADMIN)

                // Verification: Only Officers, Managers, or Admins can verify documents
                .requestMatchers(HttpMethod.PATCH, "/tourismgov/v1/touristdoc/*/documents/*/verify").hasAnyRole(OFFICER, MANAGER, ADMIN)

                // ==========================================
                // 3. FALLBACK
                // ==========================================
                .anyRequest().authenticated()
            )
            .addFilterBefore(gatewayHeaderFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Completely bypass Spring Security for public endpoints.
     * This is more reliable than .permitAll() in the filter chain.
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                .requestMatchers("/tourismgov/v1/tourist/create",
                                 "/tourismgov/v1/tourist/internal/**");
    }
}