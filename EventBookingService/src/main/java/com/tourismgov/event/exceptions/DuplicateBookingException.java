package com.tourismgov.event.exceptions;

public class DuplicateBookingException extends IllegalStateException {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DuplicateBookingException() {
        super(EventErrorMessages.DUPLICATE_BOOKING);
    }
}