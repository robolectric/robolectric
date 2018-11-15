package org.robolectric.shadows;

import android.annotation.NonNull;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.usage.UsageEvents;
import android.app.usage.UsageEvents.Event;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.app.usage.UsageStatsManager.StandbyBuckets;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Parcel;
import android.util.ArraySet;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Range;
import com.google.common.collect.SetMultimap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.HiddenApi;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/** Shadow of {@link UsageStatsManager}. */
@Implements(value = UsageStatsManager.class, minSdk = Build.VERSION_CODES.LOLLIPOP)
public class ShadowUsageStatsManager {
  private static @StandbyBuckets int currentAppStandbyBucket =
      UsageStatsManager.STANDBY_BUCKET_ACTIVE;
  private static final TreeMap<Long, Event> eventsByTimeStamp = new TreeMap<>();

  /**
   * Keys {@link UsageStats} objects by intervalType (e.g. {@link
   * UsageStatsManager#INTERVAL_WEEKLY}).
   */
  private SetMultimap<Integer, UsageStats> usageStatsByIntervalType = HashMultimap.create();

  private static final Map<String, Integer> appStandbyBuckets = new HashMap<>();

  /**
   * App usage observer registered via {@link UsageStatsManager#registerAppUsageObserver(int,
   * String[], long, TimeUnit, PendingIntent)}.
   */
  public static final class AppUsageObserver {
    private final int observerId;
    private final Collection<String> packageNames;
    private final long timeLimit;
    private final TimeUnit timeUnit;
    private final PendingIntent callbackIntent;

    public AppUsageObserver(
        int observerId,
        @NonNull Collection<String> packageNames,
        long timeLimit,
        @NonNull TimeUnit timeUnit,
        @NonNull PendingIntent callbackIntent) {
      this.observerId = observerId;
      this.packageNames = packageNames;
      this.timeLimit = timeLimit;
      this.timeUnit = timeUnit;
      this.callbackIntent = callbackIntent;
    }

    public int getObserverId() {
      return observerId;
    }

    @NonNull
    public Collection<String> getPackageNames() {
      return packageNames;
    }

    public long getTimeLimit() {
      return timeLimit;
    }

    @NonNull
    public TimeUnit getTimeUnit() {
      return timeUnit;
    }

    @NonNull
    public PendingIntent getCallbackIntent() {
      return callbackIntent;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      AppUsageObserver that = (AppUsageObserver) o;
      return observerId == that.observerId
          && timeLimit == that.timeLimit
          && packageNames.equals(that.packageNames)
          && timeUnit == that.timeUnit
          && callbackIntent.equals(that.callbackIntent);
    }

    @Override
    public int hashCode() {
      int result = observerId;
      result = 31 * result + packageNames.hashCode();
      result = 31 * result + (int) (timeLimit ^ (timeLimit >>> 32));
      result = 31 * result + timeUnit.hashCode();
      result = 31 * result + callbackIntent.hashCode();
      return result;
    }
  }

  private static final Map<Integer, AppUsageObserver> appUsageObserversById = new HashMap<>();

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
   * Adds an event to be returned by {@link UsageStatsManager#queryEvents}.
   *
   * This method won't affect the results of {@link #queryUsageStats} method.
   *
   * @deprecated Use {@link #addEvent(Event)} and {@link EventBuilder} instead.
   */
  @Deprecated
  public void addEvent(String packageName, long timeStamp, int eventType) {
    EventBuilder eventBuilder =
        EventBuilder.buildEvent()
            .setPackage(packageName)
            .setTimeStamp(timeStamp)
            .setEventType(eventType);
    if (eventType == Event.CONFIGURATION_CHANGE) {
      eventBuilder.setConfiguration(new Configuration());
    }
    addEvent(eventBuilder.build());
  }

  /**
   * Adds an event to be returned by {@link UsageStatsManager#queryEvents}.
   *
   * This method won't affect the results of {@link #queryUsageStats} method.
   *
   * The {@link Event} can be built by {@link EventBuilder}.
   */
  public void addEvent(Event event) {
    eventsByTimeStamp.put(event.getTimeStamp(), event);
  }

  /**
   * Simulates the operations done by the framework when there is a time change. If the time is
   * changed, the timestamps of all existing usage events will be shifted by the same offset as the
   * time change, in order to make sure they remain stable relative to the new time.
   *
   * This method won't affect the results of {@link #queryUsageStats} method.
   *
   * @param offsetToAddInMillis the offset to be applied to all events. For example, if {@code
   *     offsetInMillis} is 60,000, then all {@link Event}s will be shifted forward by 1 minute
   *     (into the future). Likewise, if {@code offsetInMillis} is -60,000, then all {@link Event}s
   *     will be shifted backward by 1 minute (into the past).
   */
  public void simulateTimeChange(long offsetToAddInMillis) {
    ImmutableMap.Builder<Long, Event> eventMapBuilder = ImmutableMap.builder();
    for (Event event : eventsByTimeStamp.values()) {
      long newTimestamp = event.getTimeStamp() + offsetToAddInMillis;
      eventMapBuilder.put(
          newTimestamp, EventBuilder.fromEvent(event).setTimeStamp(newTimestamp).build());
    }
    eventsByTimeStamp.putAll(eventMapBuilder.build());
  }

  /**
   * Returns aggregated UsageStats added by calling {@link #addUsageStats}.
   *
   * The real implementation creates these aggregated objects from individual {@link Event}. This
   * aggregation logic is nontrivial, so the shadow implementation just returns the aggregate data
   * added using {@link #addUsageStats}.
   */
  @Implementation
  protected List<UsageStats> queryUsageStats(int intervalType, long beginTime, long endTime) {
    List<UsageStats> results = new ArrayList<>();
    Range<Long> queryRange = Range.closed(beginTime, endTime);
    for (UsageStats stats : usageStatsByIntervalType.get(intervalType)) {
      Range<Long> statsRange = Range.closed(stats.getFirstTimeStamp(), stats.getLastTimeStamp());
      if (queryRange.isConnected(statsRange)) {
        results.add(stats);
      }
    }
    return results;
  }

  /**
   * Adds an aggregated {@code UsageStats} object, to be returned by {@link #queryUsageStats}.
   * Construct these objects with {@link UsageStatsBuilder}, and set the firstTimestamp and
   * lastTimestamp fields to make time filtering work in {@link #queryUsageStats}.
   *
   * @param intervalType An interval type constant, e.g. {@link UsageStatsManager#INTERVAL_WEEKLY}.
   */
  public void addUsageStats(int intervalType, UsageStats stats) {
    usageStatsByIntervalType.put(intervalType, stats);
  }

  /**
   * Returns the current standby bucket of the specified app that is set by {@code
   * setAppStandbyBucket}. If the standby bucket value has never been set, return {@link
   * UsageStatsManager.STANDBY_BUCKET_ACTIVE}.
   */
  @Implementation(minSdk = Build.VERSION_CODES.P)
  @HiddenApi
  public @StandbyBuckets int getAppStandbyBucket(String packageName) {
    Integer bucket = appStandbyBuckets.get(packageName);
    return (bucket == null) ? UsageStatsManager.STANDBY_BUCKET_ACTIVE : bucket;
  }

  @Implementation(minSdk = Build.VERSION_CODES.P)
  @HiddenApi
  public Map<String, Integer> getAppStandbyBuckets() {
    return new HashMap<>(appStandbyBuckets);
  }

  /** Sets the standby bucket of the specified app. */
  @Implementation(minSdk = Build.VERSION_CODES.P)
  @HiddenApi
  public void setAppStandbyBucket(String packageName, @StandbyBuckets int bucket) {
    appStandbyBuckets.put(packageName, bucket);
  }

  @Implementation(minSdk = Build.VERSION_CODES.P)
  @HiddenApi
  public void setAppStandbyBuckets(Map<String, Integer> appBuckets) {
    appStandbyBuckets.putAll(appBuckets);
  }

  @Implementation(minSdk = Build.VERSION_CODES.P)
  @HiddenApi
  protected void registerAppUsageObserver(
      int observerId,
      String[] packages,
      long timeLimit,
      TimeUnit timeUnit,
      PendingIntent callbackIntent) {
    appUsageObserversById.put(
        observerId,
        new AppUsageObserver(
            observerId, ImmutableList.copyOf(packages), timeLimit, timeUnit, callbackIntent));
  }

  @Implementation(minSdk = Build.VERSION_CODES.P)
  @HiddenApi
  protected void unregisterAppUsageObserver(int observerId) {
    appUsageObserversById.remove(observerId);
  }

  /** Returns the {@link AppUsageObserver}s currently registered in {@link UsageStatsManager}. */
  public Collection<AppUsageObserver> getRegisteredAppUsageObservers() {
    return ImmutableList.copyOf(appUsageObserversById.values());
  }

  /**
   * Triggers a currently registered {@link AppUsageObserver} with {@code observerId}.
   *
   * The observer will be no longer registered afterwards.
   */
  public void triggerRegisteredAppUsageObserver(int observerId, long timeUsedInMillis) {
    AppUsageObserver observer = appUsageObserversById.remove(observerId);
    long timeLimitInMillis = observer.timeUnit.toMillis(observer.timeLimit);
    Intent intent =
        new Intent()
            .putExtra(UsageStatsManager.EXTRA_OBSERVER_ID, observerId)
            .putExtra(UsageStatsManager.EXTRA_TIME_LIMIT, timeLimitInMillis)
            .putExtra(UsageStatsManager.EXTRA_TIME_USED, timeUsedInMillis);
    try {
      observer.callbackIntent.send(RuntimeEnvironment.application, 0, intent);
    } catch (CanceledException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns the current app's standby bucket that is set by {@code setCurrentAppStandbyBucket}. If
   * the standby bucket value has never been set, return {@link
   * UsageStatsManager.STANDBY_BUCKET_ACTIVE}.
   */
  @Implementation(minSdk = Build.VERSION_CODES.P)
  @StandbyBuckets
  protected int getAppStandbyBucket() {
    return currentAppStandbyBucket;
  }

  /** Sets the current app's standby bucket */
  public void setCurrentAppStandbyBucket(@StandbyBuckets int bucket) {
    currentAppStandbyBucket = bucket;
  }

  @Resetter
  public static void reset() {
    currentAppStandbyBucket = UsageStatsManager.STANDBY_BUCKET_ACTIVE;
    eventsByTimeStamp.clear();

    appStandbyBuckets.clear();
    appUsageObserversById.clear();
  }

  /**
   * Builder for constructing {@link UsageStats} objects. The constructor of UsageStats is not part
   * of the Android API.
   */
  public static class UsageStatsBuilder {
    private UsageStats usageStats = new UsageStats();

    // Use {@link #newBuilder} to construct builders.
    private UsageStatsBuilder() {}

    public static UsageStatsBuilder newBuilder() {
      return new UsageStatsBuilder();
    }

    public UsageStats build() {
      return usageStats;
    }

    public UsageStatsBuilder setPackageName(String packageName) {
      usageStats.mPackageName = packageName;
      return this;
    }

    public UsageStatsBuilder setFirstTimeStamp(long firstTimeStamp) {
      usageStats.mBeginTimeStamp = firstTimeStamp;
      return this;
    }

    public UsageStatsBuilder setLastTimeStamp(long lastTimeStamp) {
      usageStats.mEndTimeStamp = lastTimeStamp;
      return this;
    }

    public UsageStatsBuilder setTotalTimeInForeground(long totalTimeInForeground) {
      usageStats.mTotalTimeInForeground = totalTimeInForeground;
      return this;
    }

    public UsageStatsBuilder setLastTimeUsed(long lastTimeUsed) {
      usageStats.mLastTimeUsed = lastTimeUsed;
      return this;
    }
  }

  /**
   * Builder for constructing {@link Event} objects. The fields of Event are not part of the Android
   * API.
   */
  public static class EventBuilder {
    private Event event = new Event();

    private EventBuilder() {}

    public static EventBuilder fromEvent(Event event) {
      EventBuilder eventBuilder =
          new EventBuilder()
              .setPackage(event.mPackage)
              .setClass(event.mClass)
              .setTimeStamp(event.mTimeStamp)
              .setEventType(event.mEventType)
              .setConfiguration(event.mConfiguration);
      if (event.mEventType == Event.CONFIGURATION_CHANGE) {
        eventBuilder.setConfiguration(new Configuration());
      }
      return eventBuilder;
    }

    public static EventBuilder buildEvent() {
      return new EventBuilder();
    }

    public Event build() {
      return event;
    }

    public EventBuilder setPackage(String packageName) {
      event.mPackage = packageName;
      return this;
    }

    public EventBuilder setClass(String className) {
      event.mClass = className;
      return this;
    }

    public EventBuilder setTimeStamp(long timeStamp) {
      event.mTimeStamp = timeStamp;
      return this;
    }

    public EventBuilder setEventType(int eventType) {
      event.mEventType = eventType;
      return this;
    }

    public EventBuilder setConfiguration(Configuration configuration) {
      event.mConfiguration = configuration;
      return this;
    }

    public EventBuilder setShortcutId(String shortcutId) {
      event.mShortcutId = shortcutId;
      return this;
    }
  }
}
