package com.tourismgov.site.exceptions;

public final class SiteErrorMessages {
    private SiteErrorMessages() {}

    public static final String SITE_NOT_FOUND = "Heritage site not found with ID: %d";
    public static final String ACTIVITY_NOT_FOUND = "Preservation activity not found with ID: %d";
    public static final String DUPLICATE_SITE_NAME = "A heritage site with this name already exists.";
}