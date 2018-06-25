package org.robolectric.shadows;

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
