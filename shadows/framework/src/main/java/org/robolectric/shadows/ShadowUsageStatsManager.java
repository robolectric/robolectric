package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.app.usage.UsageEvents;
import android.app.usage.UsageEvents.Event;
import android.app.usage.UsageStatsManager;
import android.content.res.Configuration;
import android.os.Parcel;
import android.util.ArraySet;
import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/** Shadow of {@link UsageStatsManager}. */
@Implements(value = UsageStatsManager.class, minSdk = LOLLIPOP)
public class ShadowUsageStatsManager {

  private static final TreeMap<Long, Event> eventsByTimeStamp = new TreeMap<>();

  @Implementation
  protected UsageEvents queryEvents(long beginTime, long endTime) {
    List<Event> results =
        ImmutableList.copyOf(eventsByTimeStamp.subMap(beginTime, endTime).values());

    ArraySet<String> names = new ArraySet<>();
    for (Event result : results) {
      names.add(result.mPackage);
      if (result.mClass != null) {
        names.add(result.mClass);
      }
    }

    String[] table = names.toArray(new String[0]);
    Arrays.sort(table);

    // We can't directly construct usable UsageEvents, so we replicate what the framework does:
    // First the system marshalls the usage events into a Parcel.
    UsageEvents usageEvents = new UsageEvents(results, table);
    Parcel parcel = Parcel.obtain();
    usageEvents.writeToParcel(parcel, 0);
    // Then the app unmarshalls the usage events from the Parcel.
    parcel.setDataPosition(0);
    return new UsageEvents(parcel);
  }

  /**
   * Adds an event to be returned by the shadowed {@link UsageStatsManager}.
   *
   * <p>This method won't affect the results of any existing queries.
   */
  public void addEvent(String packageName, long timeStamp, int eventType) {
    Event event = new Event();
    event.mPackage = packageName;
    event.mTimeStamp = timeStamp;
    event.mEventType = eventType;
    if (eventType == Event.CONFIGURATION_CHANGE) {
      event.mConfiguration = new Configuration();
    }
    eventsByTimeStamp.put(event.getTimeStamp(), event);
  }

  @Resetter
  public static void reset() {
    eventsByTimeStamp.clear();
  }
}
