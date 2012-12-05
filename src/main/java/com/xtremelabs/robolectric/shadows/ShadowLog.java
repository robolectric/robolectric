package com.xtremelabs.robolectric.shadows;

import android.util.Log;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.io.PrintStream;
import java.util.*;

@Implements(Log.class)
public class ShadowLog {
    private static Map<String,List<LogItem>> logsByTag = new HashMap<String,List<LogItem>>();
    private static List<LogItem> logs = new ArrayList<LogItem>();
    public static PrintStream stream;

    @Implementation
    public static void e(String tag, String msg) {
        e(tag, msg, null);
    }

    @Implementation
    public static void e(String tag, String msg, Throwable throwable) {
        addLog(Log.ERROR, tag, msg, throwable);
    }

    @Implementation
    public static void d(String tag, String msg) {
        d(tag, msg, null);
    }

    @Implementation
    public static void d(String tag, String msg, Throwable throwable) {
        addLog(Log.DEBUG, tag, msg, throwable);
    }

    @Implementation
    public static void i(String tag, String msg) {
        i(tag, msg, null);
    }

    @Implementation
    public static void i(String tag, String msg, Throwable throwable) {
        addLog(Log.INFO, tag, msg, throwable);
    }

    @Implementation
    public static void v(String tag, String msg) {
        v(tag, msg, null);
    }

    @Implementation
    public static void v(String tag, String msg, Throwable throwable) {
        addLog(Log.VERBOSE, tag, msg, throwable);
    }

    @Implementation
    public static void w(String tag, String msg) {
        w(tag, msg, null);
    }

    @Implementation
    public static void w(String tag, Throwable throwable) {
        w(tag, null, throwable);
    }


    @Implementation
    public static void w(String tag, String msg, Throwable throwable) {
        addLog(Log.WARN, tag, msg, throwable);
    }

    @Implementation
    public static void wtf(String tag, String msg) {
        wtf(tag, msg, null);
    }

    @Implementation
    public static void wtf(String tag, String msg, Throwable throwable) {
        addLog(Log.ASSERT, tag, msg, throwable);
    }

    @Implementation
    public static boolean isLoggable(String tag, int level) {
        return stream != null || level >= Log.INFO;
    }

    private static void addLog(int level, String tag, String msg, Throwable throwable) {
        if (stream != null) {
            logToStream(stream, level, tag, msg, throwable);
        }
        
        LogItem item = new LogItem(level, tag, msg, throwable);
        List<LogItem> itemList = null;
        
        if (!logsByTag.containsKey(tag)) {
        	itemList = new ArrayList<LogItem>();
        	logsByTag.put(tag, itemList);
        } else {
        	itemList = logsByTag.get(tag);
        }
        
        itemList.add(item);
        logs.add(item);
    }

    private static void logToStream(PrintStream ps, int level, String tag, String msg, Throwable throwable) {
        final char c;
        switch (level) {
            case Log.ASSERT: c = 'A'; break;
            case Log.DEBUG:  c = 'D'; break;
            case Log.ERROR:  c = 'E'; break;
            case Log.WARN:   c = 'W'; break;
            case Log.INFO:   c = 'I'; break;
            case Log.VERBOSE:c = 'V'; break;
            default:         c = '?';
        }
        ps.println(c + "/" + tag + ": " + msg);
        if (throwable != null) {
            throwable.printStackTrace(ps);
        }
    }

    /**
     * Non-Android accessor.  Returns ordered list of all log entries.
     * @return
     */
    public static List<LogItem> getLogs() {
    	return logs;
    }
    
    /**
     * Non-Android accessor.  Returns ordered list of all log items for a specific tag.
     * 
     * @param tag
     * @return
     */
    public static List<LogItem> getLogsForTag( String tag ) {
    	return logsByTag.get(tag);
    }

    public static void reset() {
        logs.clear();
        logsByTag.clear();
    }

    public static class LogItem {
        public final int type;
        public final String tag;
        public final String msg;
        public final Throwable throwable;

        public LogItem(int type, String tag, String msg, Throwable throwable) {
            this.type = type;
            this.tag = tag;
            this.msg = msg;
            this.throwable = throwable;
        }
    }
}
