package com.tourismgov.event.exceptions;

public final class EventErrorMessages {
    private EventErrorMessages() {}

    public static final String EVENT_NOT_FOUND = "Event not found with ID: %d";
    public static final String BOOKING_NOT_FOUND = "Booking not found with ID: %d";
    public static final String DUPLICATE_BOOKING = "You have already confirmed a booking for this event.";
    public static final String EVENT_CAPACITY_REACHED = "This event is fully booked.";
}