package com.tourismgov.tourist.exception;
public final class TouristErrorMessage {
    private TouristErrorMessage() {}

    public static final String ERROR_TOURIST_NOT_FOUND = "Tourist not found with ID: %d";
    public static final String ERROR_DUPLICATE_CONTACT_INFO = "This email address is already registered: %s";
    public static final String ERROR_UNDERAGE_TOURIST = "Tourist must be at least 18 years old";
    
    public static final String ERROR_DOCUMENT_NOT_FOUND = "Document not found with ID: %d for Tourist ID: %d";
    public static final String ERROR_INVALID_DOCUMENT_TYPE = "Invalid document type: %s";
    public static final String ERROR_MISSING_DOCUMENT_INPUT = "Missing file or fileUri";
    public static final String ERROR_FILE_SAVE = "Could not save file: %s";
    public static final String ERROR_FILE_DELETE = "Failed to delete physical file: %s";
    public static final String ERROR_INVALID_VERIFICATION_STATUS = "Invalid status '%s'. Must be: PENDING, VERIFIED, or REJECTED";
    public static final String ERROR_DUPLICATE_DOCUMENT_TYPE = "Tourist already has a document of type %s";
    public static final String ERROR_INVALID_URI_PROTOCOL = "Invalid file URI. Only http:// or https:// are allowed.";
    public static final String ERROR_MISSING_FILE_DATA = "Upload failed: Missing physical file or file URI.";
    public static final String ERROR_FILE_SAVE_FAILED = "Internal Server Error: Failed to save the document on the server.";
}
