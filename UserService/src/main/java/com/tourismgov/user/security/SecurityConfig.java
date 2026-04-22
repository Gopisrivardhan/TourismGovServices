package com.tourismgov.user.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
class SecurityConfig {

    // SONAR FIX: Constants to prevent "String Literal Duplication" warnings
    private static final String ADMIN = "ADMIN";
    private static final String OFFICER = "OFFICER";
    private static final String MANAGER = "MANAGER";
    private static final String AUDITOR = "AUDITOR";
    private static final String COMPLIANCE = "COMPLIANCE";
    private static final String TOURIST = "TOURIST";

    private final JwtAuthenticationFilter jwtAuthFilter;

    SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) {

        http.csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                
                // 1. PUBLIC / AUTHENTICATION
            	.requestMatchers("/tourismgov/v1/users/**").permitAll()
            	.requestMatchers("/tourismgov/v1/auth/register").permitAll()
            	.requestMatchers("/tourismgov/v1/auth/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/tourismgov/tourists").permitAll()
                .requestMatchers(HttpMethod.POST, "/tourismgov/v1/auth/password/reset").hasAnyRole(ADMIN)
                .requestMatchers(HttpMethod.POST, "/tourismgov/v1/auth/password/update").hasAnyRole(ADMIN,TOURIST)
                .requestMatchers(HttpMethod.POST, "/tourismgov/v1/audit-logs/**").hasAnyRole(ADMIN)
                
                // 2. HERITAGE SITES & PRESERVATION
                .requestMatchers(HttpMethod.GET, "/tourismgov/v1/sites/**").permitAll() 
                .requestMatchers(HttpMethod.POST, "/tourismgov/v1/sites").hasAnyRole(ADMIN, MANAGER)
                .requestMatchers(HttpMethod.PUT, "/tourismgov/v1/sites/**").hasAnyRole(OFFICER, ADMIN)
                .requestMatchers(HttpMethod.POST, "/tourismgov/v1/sites/*/activities").hasAnyRole(OFFICER, MANAGER)
                .requestMatchers(HttpMethod.GET, "/tourismgov/v1/sites/*/activities").hasAnyRole(OFFICER, AUDITOR, COMPLIANCE)

                // 3. TOURIST PROFILES & DOCUMENTS
                .requestMatchers(HttpMethod.POST,"/tourismgov/v1/tourist/create").permitAll()
                .requestMatchers(HttpMethod.GET, "/tourismgov/v1/tourist/*").hasAnyRole(TOURIST,ADMIN)
                .requestMatchers(HttpMethod.DELETE, "/tourismgov/v1/tourist/**").hasAnyRole(TOURIST,ADMIN)
                .requestMatchers(HttpMethod.PUT, "/tourismgov/v1/tourist/*/update").hasRole(TOURIST)
                
                .requestMatchers(HttpMethod.GET, "/tourismgov/v1/tourist/admin/all").hasRole(ADMIN) 
                .requestMatchers(HttpMethod.POST, "/tourismgov/v1/touristdoc/*/documents").hasAnyRole(TOURIST, ADMIN)
                .requestMatchers(HttpMethod.GET, "/tourismgov/v1/touristdoc/*/documents/*/view").hasAnyRole(ADMIN, TOURIST)
                .requestMatchers(HttpMethod.PUT, "/tourismgov/v1/touristdoc/*/documents/*/verify").hasRole(ADMIN)
                .requestMatchers(HttpMethod.DELETE, "/tourismgov/v1/touristdoc/*/documents/*").hasAnyRole(TOURIST, ADMIN)

                // 4. BOOKINGS & EVENTS
                .requestMatchers(HttpMethod.GET, "/tourismgov/v1/events/**").permitAll() // Public view for tourists
                .requestMatchers(HttpMethod.POST, "/tourismgov/v1/events/*/bookings").hasAnyRole(TOURIST, OFFICER)
                .requestMatchers(HttpMethod.PATCH, "/tourismgov/v1/bookings/*/status").hasAnyRole(OFFICER, TOURIST)
                .requestMatchers(HttpMethod.GET, "/tourismgov/v1/tourists/*/bookings").hasAnyRole(TOURIST, OFFICER)
                .requestMatchers(HttpMethod.GET, "/tourismgov/v1/bookings/**").hasAnyRole(ADMIN, COMPLIANCE, AUDITOR, OFFICER)

                // 5. PROGRAM & RESOURCE MANAGEMENT
                .requestMatchers(HttpMethod.POST, "/tourismgov/v1/programs/**").hasAnyRole(MANAGER, ADMIN, OFFICER)
                .requestMatchers(HttpMethod.GET, "/tourismgov/v1/programs/**").hasAnyRole(MANAGER, ADMIN, OFFICER, AUDITOR, COMPLIANCE)
                .requestMatchers(HttpMethod.PATCH, "/tourismgov/v1/resources/**").hasAnyRole(OFFICER, MANAGER)

                // 6. NOTIFICATIONS, REPORTS & DASHBOARD
                .requestMatchers(HttpMethod.POST, "/tourismgov/v1/notifications/broadcast").hasAnyRole(ADMIN, OFFICER, MANAGER, COMPLIANCE, AUDITOR)
                .requestMatchers("/tourismgov/v1/notifications/**").authenticated() 
                .requestMatchers("/tourismgov/v1/reports/**").hasAnyRole(ADMIN, OFFICER, MANAGER, COMPLIANCE, AUDITOR) // Tourist restricted
                .requestMatchers("/tourismgov/v1/dashboard/**").authenticated()

                // 7. COMPLIANCE & AUDITS
                .requestMatchers("/tourismgov/v1/compliance/**").hasAnyRole(COMPLIANCE, ADMIN, AUDITOR)
                .requestMatchers("/tourismgov/v1/audits/**").hasAnyRole(AUDITOR, ADMIN)

                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build(); // Properly returns the built chain
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}