package com.spe.calculator;

/** Minimal JSON helpers so the app needs no external libraries. */
public final class JsonUtil {

    private JsonUtil() {
    }

    /** Formats whole numbers without a trailing ".0" (2.0 -> "2"). */
    public static String number(double value) {
        if (value == Math.rint(value) && Math.abs(value) < 1e15) {
            return Long.toString((long) value);
        }
        return Double.toString(value);
    }

    public static String escape(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }

    public static String result(String operation, double a, double b, double result) {
        return "{\"operation\":\"" + escape(operation) + "\",\"a\":" + number(a)
                + ",\"b\":" + number(b) + ",\"result\":" + number(result) + "}";
    }

    public static String error(String message, int status) {
        return "{\"error\":\"" + escape(message) + "\",\"status\":" + status + "}";
    }
}
