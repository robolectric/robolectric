package org.robolectric.shadows;

import android.util.Log;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

@Implements(Log.class)
public class ShadowLog {
  private static final int extraLogLength = "l/: \n".length();
  private static final Map<String, Queue<LogItem>> logsByTag = Collections.synchronizedMap(new
      HashMap<String, Queue<LogItem>>());
  private static final Queue<LogItem> logs = new ConcurrentLinkedQueue<>();
  public static PrintStream stream;
  private static final Map<String, Integer> tagToLevel = Collections.synchronizedMap(new
      HashMap<String, Integer>());

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
    synchronized (tagToLevel) {
      if (tagToLevel.containsKey(tag)) {
        return level >= tagToLevel.get(tag);
      }
    }
    return stream != null || level >= Log.INFO;
  }

  @Implementation
  public static int println(int priority, String tag, String msg) {
    addLog(priority, tag, msg, null);
    int tagLength = tag == null ? 0 : tag.length();
    int msgLength = msg == null ? 0 : msg.length();
    return extraLogLength + tagLength + msgLength;
  }

  /**
   * Sets the log level of a given tag, that {@link #isLoggable} will follow.
   * @param tag A log tag
   * @param level A log level, from {@link android.util.Log}
   */
  public static void setLoggable(String tag, int level) {
    tagToLevel.put(tag, level);
  }

  private static void addLog(int level, String tag, String msg, Throwable throwable) {
    if (stream != null) {
      logToStream(stream, level, tag, msg, throwable);
    }

    LogItem item = new LogItem(level, tag, msg, throwable);
    Queue<LogItem> itemList;

    synchronized (logsByTag) {
      if (!logsByTag.containsKey(tag)) {
        itemList = new ConcurrentLinkedQueue<>();
        logsByTag.put(tag, itemList);
      } else {
        itemList = logsByTag.get(tag);
      }
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
   * Returns ordered list of all log entries.
   * @return List of log items
   */
  public static List<LogItem> getLogs() {
    return new ArrayList<>(logs);
  }

  /**
   * Returns ordered list of all log items for a specific tag.
   *
   * @param tag The tag to get logs for
   * @return The list of log items for the tag
   */
  public static List<LogItem> getLogsForTag( String tag ) {
    Queue<LogItem> logs = logsByTag.get(tag);
    return logs == null ? null : new ArrayList<>(logs);
  }

  @Resetter
  public static void reset() {
    logs.clear();
    logsByTag.clear();
    tagToLevel.clear();
  }

  public static void setupLogging() {
    String logging = System.getProperty("robolectric.logging");
    if (logging != null && stream == null) {
      PrintStream stream = null;
      if ("stdout".equalsIgnoreCase(logging)) {
        stream = System.out;
      } else if ("stderr".equalsIgnoreCase(logging)) {
        stream = System.err;
      } else {
        try {
          final PrintStream file = new PrintStream(new FileOutputStream(logging), true);
          stream = file;
          Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override public void run() {
              try {
                file.close();
              } catch (Exception ignored) {
              }
            }
          });
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
      ShadowLog.stream = stream;
    }
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

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      LogItem log = (LogItem) o;
      return type == log.type
          && !(msg != null ? !msg.equals(log.msg) : log.msg != null)
          && !(tag != null ? !tag.equals(log.tag) : log.tag != null)
          && !(throwable != null ? !throwable.equals(log.throwable) : log.throwable != null);
    }

    @Override
    public int hashCode() {
      int result = type;
      result = 31 * result + (tag != null ? tag.hashCode() : 0);
      result = 31 * result + (msg != null ? msg.hashCode() : 0);
      result = 31 * result + (throwable != null ? throwable.hashCode() : 0);
      return result;
    }

    @Override
    public String toString() {
      return "LogItem{" +
          "type=" + type +
          ", tag='" + tag + '\'' +
          ", msg='" + msg + '\'' +
          ", throwable=" + throwable +
          '}';
    }
  }
}
