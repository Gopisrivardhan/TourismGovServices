package com.tourismgov.program.exceptions;

public final class ProgramErrorMessages {
    private ProgramErrorMessages() {}

    public static final String PROGRAM_NOT_FOUND = "Tourism program not found with ID: %d";
    public static final String RESOURCE_NOT_FOUND = "Resource not found with ID: %d";
    public static final String INVALID_DATE_RANGE = "The program end date cannot be earlier than the start date.";
    public static final String INSUFFICIENT_BUDGET = "Insufficient budget for this resource allocation.";
}