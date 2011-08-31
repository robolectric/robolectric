package com.xtremelabs.robolectric.shadows;

import android.util.Log;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.ArrayList;
import java.util.List;

@Implements(Log.class)
public class ShadowLog {
    private static List<LogItem> logs = new ArrayList<LogItem>();

    @Implementation
    public static void e(String tag, String msg) {
        e(tag, msg, null);
    }

    @Implementation
    public static void e(String tag, String msg, Throwable throwable) {
        logs.add(new LogItem(LogType.error, tag, msg, throwable));
    }

    @Implementation
    public static void d(String tag, String msg) {
        d(tag, msg, null);
    }

    @Implementation
    public static void d(String tag, String msg, Throwable throwable) {
        logs.add(new LogItem(LogType.debug, tag, msg, throwable));
    }

    @Implementation
    public static void i(String tag, String msg) {
        i(tag, msg, null);
    }

    @Implementation
    public static void i(String tag, String msg, Throwable throwable) {
        logs.add(new LogItem(LogType.info, tag, msg, throwable));
    }

    @Implementation
    public static void v(String tag, String msg) {
        v(tag, msg, null);
    }

    @Implementation
    public static void v(String tag, String msg, Throwable throwable) {
        logs.add(new LogItem(LogType.verbose, tag, msg, throwable));
    }

    @Implementation
    public static void w(String tag, String msg) {
        w(tag, msg, null);
    }

    @Implementation
    public static void w(String tag, String msg, Throwable throwable) {
        logs.add(new LogItem(LogType.warning, tag, msg, throwable));
    }

    @Implementation
    public static void wtf(String tag, String msg) {
        wtf(tag, msg, null);
    }

    @Implementation
    public static void wtf(String tag, String msg, Throwable throwable) {
        logs.add(new LogItem(LogType.wtf, tag, msg, throwable));
    }

    public static List<LogItem> getLogs() {
        return logs;
    }

    public static void reset() {
        logs.clear();
    }

    public static enum LogType {
        debug, error, info, verbose, warning, wtf
    }

    public static class LogItem {
        public final LogType type;
        public final String tag;
        public final String msg;
        public final Throwable throwable;

        public LogItem(LogType type, String tag, String msg, Throwable throwable) {
            this.type = type;
            this.tag = tag;
            this.msg = msg;
            this.throwable = throwable;
        }
    }
}
