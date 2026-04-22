package com.tourismgov.user.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

public final class SecurityUtils {

    // Sonar Fix: Extract hardcoded strings into constants (java:S1192)
    private static final String ANONYMOUS_USER = "anonymousUser";

    private SecurityUtils() {
        // Sonar Fix: Empty methods/constructors should be avoided. 
        // Throwing an exception prevents instantiation via Java Reflection (java:S1118 / java:S1186)
        throw new IllegalStateException("Utility class cannot be instantiated");
    }

    /**
     * Extracts the database User ID of the currently authenticated user.
     * Throws an exception if the user is not logged in.
     * * @return Long representing the current user ID
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || 
            ANONYMOUS_USER.equals(authentication.getPrincipal())) {
            
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, 
                "You must be logged in to perform this action."
            );
        }

        Object principal = authentication.getPrincipal();
        
        // Sonar Fix: Use Java Pattern Matching for 'instanceof' to avoid explicit casting (java:S6201)
        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getUserId();
        }

        throw new ResponseStatusException(
            HttpStatus.INTERNAL_SERVER_ERROR, 
            "Security context error: Could not verify user identity."
        );
    }
}