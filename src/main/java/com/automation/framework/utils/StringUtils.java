package com.automation.framework.utils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Small string-splitting helpers used when a property or test-data field
 * carries multiple values in one cell/line - e.g. a comma-separated tag
 * list, or a newline-separated block copied from a multipart email body.
 */
public final class StringUtils {

    private StringUtils() {
    }

    /** Splits a comma-separated value into a trimmed, non-empty list. */
    public static List<String> splitCsv(String value) {
        return splitAndTrim(value, ",");
    }

    /** Splits a newline-separated block into a trimmed, non-empty list. */
    public static List<String> splitLines(String value) {
        return splitAndTrim(value, "\\R");
    }

    private static List<String> splitAndTrim(String value, String delimiterRegex) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(delimiterRegex))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }
}
