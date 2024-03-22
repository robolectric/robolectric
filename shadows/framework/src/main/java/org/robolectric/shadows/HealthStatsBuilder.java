package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Parcel;
import android.os.health.HealthStats;
import android.os.health.TimerStat;
import android.util.ArrayMap;
import com.google.common.annotations.VisibleForTesting;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Test helper class to build {@link HealthStats} */
final class HealthStatsBuilder {

  // Header fields
  private String dataType = null;

  // TimerStat fields
  private final HashMap<Integer, TimerStat> timerMap = new HashMap<>();

  // Measurement fields
  private final HashMap<Integer, Long> measurementMap = new HashMap<>();

  // Stats fields
  private final HashMap<Integer, ArrayMap<String, HealthStats>> statsMap = new HashMap<>();

  // Timers fields
  private final HashMap<Integer, ArrayMap<String, TimerStat>> timersMap = new HashMap<>();

  // Measurements fields
  private final HashMap<Integer, ArrayMap<String, Long>> measurementsMap = new HashMap<>();

  public static HealthStatsBuilder newBuilder() {
    return new HealthStatsBuilder();
  }

  /** Sets the DataType. Defaults to null. */
  @CanIgnoreReturnValue
  public HealthStatsBuilder setDataType(String dataType) {
    this.dataType = dataType;
    return this;
  }

  /**
   * Adds a TimerStat for the given key. If the same key is used multiple times, the last provided
   * TimerStat is used.
   */
  @CanIgnoreReturnValue
  public HealthStatsBuilder addTimerStat(int key, TimerStat value) {
    timerMap.put(key, value);
    return this;
  }

  /**
   * Adds a measurement for the given key. If the same key is used multiple times, the last provided
   * measurement is used.
   */
  @CanIgnoreReturnValue
  public HealthStatsBuilder addMeasurement(int key, long value) {
    measurementMap.put(key, value);
    return this;
  }

  /**
   * Adds a map of HealthStats for the given key. If the same key is used multiple times, the last
   * provided map is used.
   */
  @CanIgnoreReturnValue
  public HealthStatsBuilder addStats(int key, ArrayMap<String, HealthStats> value) {
    statsMap.put(key, new ArrayMap<String, HealthStats>(value));
    return this;
  }

  /**
   * Adds a map of TimerStats for the given key. If the same key is used multiple times, the last
   * provided map is used.
   */
  @CanIgnoreReturnValue
  public HealthStatsBuilder addTimers(int key, ArrayMap<String, TimerStat> value) {
    timersMap.put(key, new ArrayMap<String, TimerStat>(value));
    return this;
  }

  /**
   * Adds a map of measurements for the given key. If the same key is used multiple times, the last
   * provided map is used.
   */
  @CanIgnoreReturnValue
  public HealthStatsBuilder addMeasurements(int key, ArrayMap<String, Long> value) {
    measurementsMap.put(key, new ArrayMap<String, Long>(value));
    return this;
  }

  // HealthStats has internal fields that are generic arrays, so this is unavoidable.
  @SuppressWarnings("unchecked")
  public HealthStats build() {
    // HealthStats' default constructor throws an exception
    HealthStats result = new HealthStats(Parcel.obtain());

    reflector(HealthStatsReflector.class, result).setDataType(dataType);

    int[] mTimerKeys = toSortedIntArray(timerMap.keySet());
    reflector(HealthStatsReflector.class, result).setTimerKeys(mTimerKeys);
    int[] mTimerCounts = new int[mTimerKeys.length];
    long[] mTimerTimes = new long[mTimerKeys.length];
    for (int i = 0; i < mTimerKeys.length; i++) {
      TimerStat timerStat = timerMap.get(mTimerKeys[i]);
      mTimerCounts[i] = timerStat.getCount();
      mTimerTimes[i] = timerStat.getTime();
    }
    reflector(HealthStatsReflector.class, result).setTimerCounts(mTimerCounts);
    reflector(HealthStatsReflector.class, result).setTimerTimes(mTimerTimes);

    int[] mMeasurementKeys = toSortedIntArray(measurementMap.keySet());
    reflector(HealthStatsReflector.class, result).setMeasurementKeys(mMeasurementKeys);
    long[] mMeasurementValues = new long[mMeasurementKeys.length];
    for (int i = 0; i < mMeasurementKeys.length; i++) {
      long measurementValue = measurementMap.get(mMeasurementKeys[i]);
      mMeasurementValues[i] = measurementValue;
    }
    reflector(HealthStatsReflector.class, result).setMeasurementValues(mMeasurementValues);

    int[] mStatsKeys = toSortedIntArray(statsMap.keySet());
    reflector(HealthStatsReflector.class, result).setStatsKeys(mStatsKeys);
    ArrayMap<String, HealthStats>[] mStatsValues =
        (ArrayMap<String, HealthStats>[]) new ArrayMap<?, ?>[mStatsKeys.length];
    for (int i = 0; i < mStatsKeys.length; i++) {
      ArrayMap<String, HealthStats> stats = statsMap.get(mStatsKeys[i]);
      mStatsValues[i] = stats;
    }
    reflector(HealthStatsReflector.class, result).setStatsValues(mStatsValues);

    int[] mTimersKeys = toSortedIntArray(timersMap.keySet());
    reflector(HealthStatsReflector.class, result).setTimersKeys(mTimersKeys);
    ArrayMap<String, TimerStat>[] mTimersValues =
        (ArrayMap<String, TimerStat>[]) new ArrayMap<?, ?>[mTimersKeys.length];
    for (int i = 0; i < mTimersKeys.length; i++) {
      ArrayMap<String, TimerStat> timers = timersMap.get(mTimersKeys[i]);
      mTimersValues[i] = timers;
    }
    reflector(HealthStatsReflector.class, result).setTimersValues(mTimersValues);

    int[] mMeasurementsKeys = toSortedIntArray(measurementsMap.keySet());
    reflector(HealthStatsReflector.class, result).setMeasurementsKeys(mMeasurementsKeys);
    ArrayMap<String, Long>[] mMeasurementsValues =
        (ArrayMap<String, Long>[]) new ArrayMap<?, ?>[mMeasurementsKeys.length];
    for (int i = 0; i < mMeasurementsKeys.length; i++) {
      ArrayMap<String, Long> measurements = measurementsMap.get(mMeasurementsKeys[i]);
      mMeasurementsValues[i] = measurements;
    }
    reflector(HealthStatsReflector.class, result).setMeasurementsValues(mMeasurementsValues);

    return result;
  }

  private HealthStatsBuilder() {}

  @ForType(HealthStats.class)
  private interface HealthStatsReflector {

    @Accessor("mDataType")
    void setDataType(String dataType);

    @Accessor("mTimerKeys")
    void setTimerKeys(int[] timerKeys);

    @Accessor("mTimerCounts")
    void setTimerCounts(int[] timerCounts);

    @Accessor("mTimerTimes")
    void setTimerTimes(long[] timerTimes);

    @Accessor("mMeasurementKeys")
    void setMeasurementKeys(int[] measurementKeys);

    @Accessor("mMeasurementValues")
    void setMeasurementValues(long[] measurementValues);

    @Accessor("mStatsKeys")
    void setStatsKeys(int[] statsKeys);

    @Accessor("mStatsValues")
    void setStatsValues(ArrayMap<String, HealthStats>[] statsValues);

    @Accessor("mTimersKeys")
    void setTimersKeys(int[] timersKeys);

    @Accessor("mTimersValues")
    void setTimersValues(ArrayMap<String, TimerStat>[] timersValues);

    @Accessor("mMeasurementsKeys")
    void setMeasurementsKeys(int[] measurementsKeys);

    @Accessor("mMeasurementsValues")
    void setMeasurementsValues(ArrayMap<String, Long>[] measurementsValues);
  }

  @VisibleForTesting
  static final int[] toSortedIntArray(Set<Integer> set) {
    int[] result = new int[set.size()];
    Object[] inputObjArray = set.toArray();
    for (int i = 0; i < inputObjArray.length; i++) {
      result[i] = (int) inputObjArray[i];
    }
    // mFooKeys fields of HealthStats are used in Arrays.binarySearch, so they have to be sorted.
    Arrays.sort(result);
    return result;
  }
}
