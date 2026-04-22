package com.tourismgov.user.exceptions;

public final class SecurityErrorMessages {
    private SecurityErrorMessages() {}

    public static final String USER_NOT_FOUND = "User not found with ID: %d";
    public static final String INVALID_OLD_PASSWORD = "Old password is incorrect";
    public static final String UNAUTHORIZED_ACTION = "You do not have permission to do this";
    public static final String ACCOUNT_BLOCKED = "This account has been blocked.";
}