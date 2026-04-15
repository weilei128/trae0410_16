package com.example.messageboard.util;

import java.util.regex.Pattern;

public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    private ValidationUtil() {
    }

    public static boolean isEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return true;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        int length = username.trim().length();
        return length >= 1 && length <= 50;
    }

    public static boolean isValidContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        int length = content.trim().length();
        return length >= 1 && length <= 2000;
    }

    public static String truncate(String str, int maxLength) {
        if (str == null) {
            return null;
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength);
    }
}
