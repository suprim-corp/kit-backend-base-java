package dev.suprim.kit.core;

import java.text.Normalizer;
import java.util.Collection;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Data manipulation and validation utilities.
 */
public final class DataUtils {

    private static final Pattern DIACRITICAL_MARKS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    private DataUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Checks if an object is null.
     */
    public static boolean isNull(Object obj) {
        return Objects.isNull(obj);
    }

    /**
     * Checks if an object is not null.
     */
    public static boolean nonNull(Object obj) {
        return Objects.nonNull(obj);
    }

    /**
     * Checks if a string is null or empty.
     */
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * Checks if a string is null or blank (empty or whitespace only).
     */
    public static boolean isNullOrBlank(String str) {
        return str == null || str.isBlank();
    }

    /**
     * Checks if a collection is null or empty.
     */
    public static boolean isNullOrEmpty(Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /**
     * Returns the first non-null value, or null if all are null.
     */
    @SafeVarargs
    public static <T> T coalesce(T... values) {
        if (values == null) return null;
        for (T value : values) {
            if (value != null) return value;
        }
        return null;
    }

    /**
     * Returns the string if not blank, otherwise returns the default value.
     */
    public static String defaultIfBlank(String str, String defaultValue) {
        return isNullOrBlank(str) ? defaultValue : str;
    }

    /**
     * Removes diacritical marks (accents) from Vietnamese and other text.
     * Converts "Nguyễn Văn An" to "Nguyen Van An".
     */
    public static String removeAccents(String str) {
        if (isNullOrEmpty(str)) return str;

        String normalized = str.trim().replaceAll(" +", " ");

        // Handle Vietnamese specific characters
        normalized = normalized
            .replace("đ", "d")
            .replace("Đ", "D");

        // Normalize and remove diacritical marks
        String nfd = Normalizer.normalize(normalized, Normalizer.Form.NFD);
        return DIACRITICAL_MARKS.matcher(nfd).replaceAll("");
    }

    /**
     * Converts a string to lowercase safely.
     */
    public static String toLowerCase(String str) {
        return str == null ? null : str.toLowerCase();
    }

    /**
     * Converts a string to uppercase safely.
     */
    public static String toUpperCase(String str) {
        return str == null ? null : str.toUpperCase();
    }

    /**
     * Trims a string safely.
     */
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    /**
     * Parses a string to Integer, returns null if parsing fails.
     */
    public static Integer parseIntOrNull(String str) {
        if (isNullOrBlank(str)) return null;
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parses a string to Long, returns null if parsing fails.
     */
    public static Long parseLongOrNull(String str) {
        if (isNullOrBlank(str)) return null;
        try {
            return Long.parseLong(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parses a string to Double, returns null if parsing fails.
     */
    public static Double parseDoubleOrNull(String str) {
        if (isNullOrBlank(str)) return null;
        try {
            return Double.parseDouble(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Parses a string to Boolean. Returns true for "true", "1", "yes", "on" (case insensitive).
     */
    public static Boolean parseBooleanOrNull(String str) {
        if (isNullOrBlank(str)) return null;
        String lower = str.trim().toLowerCase();
        return "true".equals(lower) || "1".equals(lower) || "yes".equals(lower) || "on".equals(lower);
    }
}
