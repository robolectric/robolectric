package org.robolectric.shadows;


import android.annotation.NonNull;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.usage.UsageEvents;
import android.app.usage.UsageEvents.Event;
import android.app.usage.UsageStatsManager;
import android.app.usage.UsageStatsManager.StandbyBuckets;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Parcel;
import android.util.ArraySet;
import com.google.common.collect.ImmutableList;
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

  
  /**
   * Returns the current standby bucket of the specified app that is set by {@code
   * setAppStandbyBucket}. If the standby bucket value has never been set, return {@link
   * UsageStatsManager.STANDBY_BUCKET_ACTIVE}.
   */
  @Implementation(minSdk = Build.VERSION_CODES.P)
  public @StandbyBuckets int getAppStandbyBucket(String packageName) {
    // This check is to mimic the real version so tests crash/fail if this is called on older
    // platform versions as the real code would.
    Integer bucket = appStandbyBuckets.get(packageName);
    return (bucket == null) ? UsageStatsManager.STANDBY_BUCKET_ACTIVE : bucket;
  }

  /**
   * Sets the standby bucket of the specified app.
   */
  @Implementation(minSdk = Build.VERSION_CODES.P)
  public void setAppStandbyBucket(String packageName, @StandbyBuckets int bucket) {
    appStandbyBuckets.put(packageName, bucket);
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
   * <p>The observer will be no longer registered afterwards.
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
  public @StandbyBuckets int getAppStandbyBucket() {
    return currentAppStandbyBucket;
  }

  /**
   * Sets the current app's standby bucket
   */
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
}
