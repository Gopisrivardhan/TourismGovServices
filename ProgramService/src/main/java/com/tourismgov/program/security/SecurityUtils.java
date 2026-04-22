package com.tourismgov.program.security;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

public final class SecurityUtils {

    private static final String ANONYMOUS_USER = "anonymousUser";

    private SecurityUtils() {
        throw new IllegalStateException("Utility class cannot be instantiated");
    }

    /**
     * Extracts the User ID passed down from the API Gateway header filter.
     * @return Long representing the current user ID
     */
    public static Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 1. Check if the user is actually authenticated
        if (authentication == null || !authentication.isAuthenticated() || 
            ANONYMOUS_USER.equals(authentication.getPrincipal())) {
            
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED, 
                "You must be logged in to perform this action."
            );
        }

        // 2. Safely extract the ID
        if (authentication.getPrincipal() instanceof Long userId) {
            return userId;
        } else if (authentication.getPrincipal() instanceof String userIdStr) {
             try {
                 return Long.parseLong(userIdStr);
             } catch (NumberFormatException e) {
                 throw new ResponseStatusException(
                     HttpStatus.INTERNAL_SERVER_ERROR, 
                     "Security context error: Invalid User ID format."
                 );
             }
        }

        // 3. Fallback error if the context got corrupted
        throw new ResponseStatusException(
            HttpStatus.INTERNAL_SERVER_ERROR, 
            "Security context error: Could not verify user identity."
        );
    }
}