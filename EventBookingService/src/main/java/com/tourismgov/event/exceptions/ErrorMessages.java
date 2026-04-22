package com.tourismgov.event.exceptions;

public final class ErrorMessages {

    private ErrorMessages() {
        throw new IllegalStateException("Utility class cannot be instantiated");
    }

    public static final String DUPLICATE_BOOKING = "You have already booked a ticket for this event.";
    public static final String UNAUTHORIZED_ACTION = "You do not have permission to perform this action.";
    public static final String RESOURCE_NOT_FOUND = "The requested resource could not be found.";
    public static final String INVALID_STATUS = "The provided status is invalid.";

}