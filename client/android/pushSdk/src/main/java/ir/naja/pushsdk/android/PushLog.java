package ir.naja.pushsdk.android;

import android.util.Log;

public final class PushLog {
    public static final String TAG = "NajiPush";

    private PushLog() {
    }

    public static void i(String message) {
        Log.i(TAG, message);
    }

    public static void w(String message) {
        Log.w(TAG, message);
    }

    public static void e(String message) {
        Log.e(TAG, message);
    }

    public static void e(String message, Throwable error) {
        Log.e(TAG, message, error);
    }

    static String preview(String value) {
        if (value == null) {
            return "null";
        }
        int length = value.length();
        if (length == 0) {
            return "empty";
        }
        if (length <= 8) {
            return "len=" + length;
        }
        return "len=" + length + " head=" + value.substring(0, 8);
    }

    static String errorSummary(Throwable error) {
        if (error == null) {
            return "null";
        }
        StringBuilder builder = new StringBuilder();
        Throwable current = error;
        int depth = 0;
        while (current != null && depth < 4) {
            if (depth > 0) {
                builder.append(" | cause=");
            }
            builder.append(current.getClass().getSimpleName())
                    .append(": ")
                    .append(current.getMessage());
            current = current.getCause();
            depth++;
        }
        return builder.toString();
    }
}
