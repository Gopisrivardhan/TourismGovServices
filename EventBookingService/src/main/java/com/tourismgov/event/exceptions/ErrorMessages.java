package com.tourismgov.event.exceptions;

public final class ErrorMessages {

    private ErrorMessages() {
        throw new IllegalStateException("Utility class cannot be instantiated");
    }

    public static final String DUPLICATE_BOOKING = "You have already booked a ticket for this event.";
    public static final String UNAUTHORIZED_ACTION = "You do not have permission to perform this action.";
    public static final String RESOURCE_NOT_FOUND = "The requested resource could not be found.";
    public static final String INVALID_STATUS = "The provided status is invalid.";
    
    // Added specific messages for Event Service validations
    public static final String SITE_SERVICE_ERROR = "Error communicating with Site Service.";
    public static final String PROGRAM_SERVICE_ERROR = "Error communicating with Program Service.";
    public static final String EVENT_DATE_OUT_OF_BOUNDS = "Event date (%s) is out of bounds for the assigned Tourism Program. Allowed dates: %s to %s";
}