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

  /**
   * Whether calling {@link Log#wtf} will throw {@link TerribleFailure}. This is analogous to
   * Android's {@link android.provider.Settings.Global#WTF_IS_FATAL}. The default value is false to
   * preserve existing behavior.
   */
  private static boolean wtfIsFatal = false;

  @Implementation
  protected static int e(String tag, String msg) {
    return e(tag, msg, null);
  }

  @Implementation
  protected static int e(String tag, String msg, Throwable throwable) {
    return addLog(Log.ERROR, tag, msg, throwable);
  }

  @Implementation
  protected static int d(String tag, String msg) {
    return d(tag, msg, null);
  }

  @Implementation
  protected static int d(String tag, String msg, Throwable throwable) {
    return addLog(Log.DEBUG, tag, msg, throwable);
  }

  @Implementation
  protected static int i(String tag, String msg) {
    return i(tag, msg, null);
  }

  @Implementation
  protected static int i(String tag, String msg, Throwable throwable) {
    return addLog(Log.INFO, tag, msg, throwable);
  }

  @Implementation
  protected static int v(String tag, String msg) {
    return v(tag, msg, null);
  }

  @Implementation
  protected static int v(String tag, String msg, Throwable throwable) {
    return addLog(Log.VERBOSE, tag, msg, throwable);
  }

  @Implementation
  protected static int w(String tag, String msg) {
    return w(tag, msg, null);
  }

  @Implementation
  protected static int w(String tag, Throwable throwable) {
    return w(tag, null, throwable);
  }

  @Implementation
  protected static int w(String tag, String msg, Throwable throwable) {
    return addLog(Log.WARN, tag, msg, throwable);
  }

  @Implementation
  protected static int wtf(String tag, String msg) {
    return wtf(tag, msg, null);
  }

  @Implementation
  protected static int wtf(String tag, String msg, Throwable throwable) {
    addLog(Log.ASSERT, tag, msg, throwable);
    if (wtfIsFatal) {
      throw new TerribleFailure(msg, throwable);
    }
    return 0;
  }

  /** Sets whether calling {@link Log#wtf} will throw {@link TerribleFailure}. */
  public static void setWtfIsFatal(boolean fatal) {
    wtfIsFatal = fatal;
  }

  @Implementation
  protected static boolean isLoggable(String tag, int level) {
    synchronized (tagToLevel) {
      if (tagToLevel.containsKey(tag)) {
        return level >= tagToLevel.get(tag);
      }
    }
    return stream != null || level >= Log.INFO;
  }

  @Implementation
  protected static int println_native(int bufID, int priority, String tag, String msg) {
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

  private static int addLog(int level, String tag, String msg, Throwable throwable) {
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

    return 0;
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
   * @return The list of log items for the tag or an empty list if no logs for that tag exist.
   */
  public static List<LogItem> getLogsForTag(String tag) {
    Queue<LogItem> logs = logsByTag.get(tag);
    return logs == null ? Collections.emptyList() : new ArrayList<>(logs);
  }

  /** Clear all accumulated logs. */
  public static void clear() {
    reset();
  }

  @Resetter
  public static void reset() {
    logs.clear();
    logsByTag.clear();
    tagToLevel.clear();
    wtfIsFatal = false;
  }

  @SuppressWarnings("CatchAndPrintStackTrace")
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

  /**
   * Failure thrown when wtf_is_fatal is true and Log.wtf is called. This is a parallel
   * implementation of framework's hidden API {@link android.util.Log#TerribleFailure}, to allow
   * tests to catch / expect these exceptions.
   */
  public static class TerribleFailure extends RuntimeException {
    public TerribleFailure(String msg, Throwable cause) {
      super(msg, cause);
    }
  }
}
