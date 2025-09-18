package com.academy.utils;

public final class FieldLengths {

    private FieldLengths() {
        // Prevent instantiation
    }

    // Reviews
    public static final int REVIEW_MAX = 1000;

    // Addresses & contact info
    public static final int ADDRESS_MAX = 255;
    public static final int POSTAL_CODE_MAX = 20;
    public static final int PHONE_NUMBER_MAX = 20;

    // User info
    public static final int EMAIL_MAX = 254;
    public static final int PASSWORD_MAX = 64;
    public static final int USERNAME_MAX = 20;

    // Service info
    public static final int SERVICE_TITLE_MAX = 60;
    public static final int SERVICE_DESCRIPTION_MAX = 2000;
    public static final int SERVICE_ENTITY_MAX = 10;

    public static final int SERVICE_TYPE_MAX = 50;

    // Tag info
    public static final int TAG_NAME_MAX = 30;
    public static final int MAX_SERVICE_TAGS = 50;

    // URLs
    public static final int URL_MAX = 2048;

    // Notifications
    public static final int NOTIFICATION_TITLE_MAX = 100;
    public static final int NOTIFICATION_BODY_MAX = 240;

    // Role
    public static final int ROLE_MAX = 30;

    //Enum max length
    public static final int ENUM_MAX_LENGTH = 50;
}