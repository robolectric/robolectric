package org.robolectric.shadows;

import android.os.Build.VERSION_CODES;
import android.util.EventLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;

@Implements(EventLog.class)
public class ShadowEventLog {

  /**
   * Constant written to the log if the parameter is null.
   *
   * <p>This matches how the real android code handles nulls.
   */
  static final String NULL_PLACE_HOLDER = "NULL";

  @Implements(EventLog.Event.class)
  public static class ShadowEvent {

    private Object data;
    private int tag;
    private int processId;
    private long timeNanos;

    @Implementation
    protected Object getData() {
      return data;
    }

    @Implementation
    protected int getTag() {
      return tag;
    }

    @Implementation
    protected int getProcessId() {
      return processId;
    }

    @Implementation
    protected long getTimeNanos() {
      return timeNanos;
    }
  }

  private static final List<EventLog.Event> events = new ArrayList<>();

  /** Class to build {@link EventLog.Event} */
  public static class EventBuilder {

    private final Object data;
    private final int tag;
    private int processId = ShadowProcess.myPid();
    private long timeNanos = System.nanoTime();

    public EventBuilder(int tag, Object data) {
      this.tag = tag;
      this.data = data;
    }

    public EventBuilder setProcessId(int processId) {
      this.processId = processId;
      return this;
    }

    public EventBuilder setTimeNanos(long timeNanos) {
      this.timeNanos = timeNanos;
      return this;
    }

    public EventLog.Event build() {
      EventLog.Event event = Shadow.newInstanceOf(EventLog.Event.class);
      ShadowEvent shadowEvent = Shadow.extract(event);
      shadowEvent.data = data;
      shadowEvent.tag = tag;
      shadowEvent.processId = processId;
      shadowEvent.timeNanos = timeNanos;
      return event;
    }
  }

  /** Add event to {@link EventLog}. */
  public static void addEvent(EventLog.Event event) {
    events.add(event);
  }

  @Resetter
  public static void clearAll() {
    events.clear();
  }

  /** Writes an event log message, returning an approximation of the bytes written. */
  @Implementation
  protected static int writeEvent(int tag, String str) {
    if (str == null) {
      str = NULL_PLACE_HOLDER;
    }
    addEvent(new EventBuilder(tag, str).build());
    return Integer.BYTES + str.length();
  }

  /** Writes an event log message, returning an approximation of the bytes written. */
  @Implementation
  protected static int writeEvent(int tag, Object... list) {
    if (list == null) {
      // This matches how the real android code handles nulls
      return writeEvent(tag, (String) null);
    }
    addEvent(new EventBuilder(tag, list).build());
    return Integer.BYTES + list.length * Integer.BYTES;
  }

  /** Writes an event log message, returning an approximation of the bytes written. */
  @Implementation
  protected static int writeEvent(int tag, int value) {
    addEvent(new EventBuilder(tag, value).build());
    return Integer.BYTES + Integer.BYTES;
  }

  /** Writes an event log message, returning an approximation of the bytes written. */
  @Implementation(minSdk = VERSION_CODES.M)
  protected static int writeEvent(int tag, float value) {
    addEvent(new EventBuilder(tag, value).build());
    return Integer.BYTES + Float.BYTES;
  }

  /** Writes an event log message, returning an approximation of the bytes written. */
  @Implementation
  protected static int writeEvent(int tag, long value) {
    addEvent(new EventBuilder(tag, value).build());
    return Integer.BYTES + Long.BYTES;
  }

  @Implementation
  protected static void readEvents(int[] tags, Collection<EventLog.Event> output) {
    for (EventLog.Event event : events) {
      for (int tag : tags) {
        if (tag == event.getTag()) {
          output.add(event);
          break;
        }
      }
    }
  }
}
