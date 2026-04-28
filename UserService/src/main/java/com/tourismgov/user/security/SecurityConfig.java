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
public class SecurityConfig {

    // Define your Role constants for easy mapping
    private static final String ADMIN = "ADMIN";
    private static final String MANAGER = "MANAGER";
    private static final String AUDITOR = "AUDITOR";
    private static final String COMPLIANCE = "COMPLIANCE";

    // Injecting your working JWT Filter instead of the Gateway header filter
    private final JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                
                // ==========================================
                // 1. AUTHENTICATION ENDPOINTS (/tourismgov/v1/auth)
                // ==========================================
                // Public Access: Anyone can login, register, or request a password reset
                .requestMatchers(HttpMethod.POST, "/tourismgov/v1/auth/login", "/tourismgov/v1/auth/register").permitAll()
                .requestMatchers(HttpMethod.PUT, "/tourismgov/v1/auth/password/reset").permitAll()
                
                // Authenticated: A user must be logged in to update their known password
                .requestMatchers(HttpMethod.PUT, "/tourismgov/v1/auth/password/update").authenticated()

                // ==========================================
                // 2. USER MANAGEMENT ENDPOINTS (/tourismgov/v1/users)
                // ==========================================
                .requestMatchers(HttpMethod.POST, "/tourismgov/v1/users").permitAll()
                // GET /users and GET /users/{id} are also called by other microservices via Feign
                .requestMatchers(HttpMethod.GET, "/tourismgov/v1/users").permitAll()
                .requestMatchers(HttpMethod.GET, "/tourismgov/v1/users/{id}").permitAll()

                // ==========================================
                // 3. AUDIT LOG ENDPOINTS (/tourismgov/v1/audit-logs)
                // ==========================================
                .requestMatchers(HttpMethod.POST, "/tourismgov/v1/audit-logs").permitAll()
                .requestMatchers(HttpMethod.GET,  "/tourismgov/v1/audit-logs").hasAnyRole(ADMIN, AUDITOR, COMPLIANCE)
                .requestMatchers(HttpMethod.GET,  "/tourismgov/v1/audit-logs/user/*").hasAnyRole(ADMIN, AUDITOR, COMPLIANCE)
                .requestMatchers(HttpMethod.GET,  "/tourismgov/v1/audit-logs/action/*").hasAnyRole(ADMIN, AUDITOR, COMPLIANCE)
                .requestMatchers(HttpMethod.GET,  "/tourismgov/v1/audit-logs/dates").hasAnyRole(ADMIN, AUDITOR, COMPLIANCE)

                // Fallback: Any other request must be authenticated
                .anyRequest().authenticated()
            );

        // Add your custom JWT filter before the standard Spring Security filter
        http.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    // Your working Authentication Manager
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // Your working Password Encoder
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}