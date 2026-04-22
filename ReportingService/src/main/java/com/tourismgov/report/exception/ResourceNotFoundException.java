package com.tourismgov.report.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * ResourceNotFoundException - Standard 404 error wrapper.
 * We use Object for the ID to support both Long and String identifiers.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    // 1. For custom messages (e.g., "user is not exist")
    public ResourceNotFoundException(String message) {
        super(message);
    }

    // 2. For standard "Resource X not found with ID Y" messages
    // Using Object allows you to pass Long, String, or UUID
    public ResourceNotFoundException(String resourceName, Object id) {
        super(String.format("%s not found with id: %s", resourceName, id));
    }
}