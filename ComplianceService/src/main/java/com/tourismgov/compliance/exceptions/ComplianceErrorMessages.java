package com.tourismgov.compliance.exceptions;

public final class ComplianceErrorMessages {
    private ComplianceErrorMessages() {}

    public static final String RECORD_NOT_FOUND = "Compliance record not found with ID: %d";
    public static final String AUDIT_NOT_FOUND = "Audit not found with ID: %d";
    public static final String INVALID_AUDIT_STATE = "Cannot modify an audit that is already COMPLETED.";
}