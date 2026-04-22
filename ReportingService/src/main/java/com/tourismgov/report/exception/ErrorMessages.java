package com.tourismgov.report.exception;

/**
 * ErrorMessages - Centralized dictionary for API error responses.
 * Private constructor prevents instantiation.
 */
public final class ErrorMessages {

    private ErrorMessages() {}

    // 1. Identity & User Messages (The core requirement)
    public static final String USER_NOT_EXIST = "user is not exist"; // EXACT string required by UI
    public static final String TOURIST_NOT_AUTHORIZED = "Access Denied: Tourists are not authorized to generate government reports.";
    
    // 2. Resource Specific
    public static final String REPORT_NOT_FOUND = "Report not found";
    public static final String EVENT_NOT_FOUND = "Event not found";
    public static final String SITE_NOT_FOUND = "Heritage site not found";
    public static final String RECORD_NOT_FOUND = "Record not found";

    // 3. System & Communication
    public static final String EXTERNAL_SERVICE_DOWN = "An external service is currently unreachable.";
    public static final String UNAUTHORIZED_ACTION = "You do not have permission to do this";
    public static final String MALFORMED_JSON = "Malformed JSON request or invalid data types provided.";
}