package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;
import static com.google.common.truth.Truth.assertThat;

import android.os.health.HealthStats;
import android.os.health.TimerStat;
import android.util.ArrayMap;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.primitives.Ints;
import java.util.HashSet;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
@Config(minSdk = N)
public class HealthStatsBuilderTest {

  private static final int KEY_1 = 867;
  private static final int KEY_2 = 5309;

  private static final TimerStat TIMER_STAT_1 = new TimerStat(42, 64);
  private static final TimerStat TIMER_STAT_2 = new TimerStat(5, 7);

  private static final long MEASUREMENT_1 = 13;
  private static final long MEASUREMENT_2 = 17;

  private static final String MAP_STRING_1 = "map_string_1";
  private static final String MAP_STRING_2 = "map_string_2";

  private static final ArrayMap<String, Long> MEASUREMENTS_MAP = new ArrayMap<>();
  private static final long MEASUREMENTS_VALUE_1 = 19;
  private static final long MEASUREMENTS_VALUE_2 = 21;

  private static final ArrayMap<String, HealthStats> STATS_MAP = new ArrayMap<>();
  private static final HealthStats STATS_VALUE_1 =
      HealthStatsBuilder.newBuilder().setDataType("23").build();
  private static final HealthStats STATS_VALUE_2 =
      HealthStatsBuilder.newBuilder().setDataType("27").build();

  private static final ArrayMap<String, TimerStat> TIMERS_MAP = new ArrayMap<>();
  private static final TimerStat TIMERS_VALUE_1 = new TimerStat(29, 31);
  private static final TimerStat TIMERS_VALUE_2 = new TimerStat(37, 41);

  static {
    MEASUREMENTS_MAP.put(MAP_STRING_1, MEASUREMENTS_VALUE_1);
    MEASUREMENTS_MAP.put(MAP_STRING_2, MEASUREMENTS_VALUE_2);

    STATS_MAP.put(MAP_STRING_1, STATS_VALUE_1);
    STATS_MAP.put(MAP_STRING_2, STATS_VALUE_2);

    TIMERS_MAP.put(MAP_STRING_1, TIMERS_VALUE_1);
    TIMERS_MAP.put(MAP_STRING_2, TIMERS_VALUE_2);
  }

  @Test
  public void emptyBuilder_isEmpty() {
    HealthStats stats = HealthStatsBuilder.newBuilder().build();

    assertThat(stats.getDataType()).isNull();
    assertThat(stats.getMeasurementKeyCount()).isEqualTo(0);
    assertThat(stats.getMeasurementsKeyCount()).isEqualTo(0);
    assertThat(stats.getStatsKeyCount()).isEqualTo(0);
    assertThat(stats.getTimerKeyCount()).isEqualTo(0);
    assertThat(stats.getTimersKeyCount()).isEqualTo(0);
  }

  @Test
  public void setEverything_everythingSetsCorrectly() {
    HealthStats stats =
        HealthStatsBuilder.newBuilder()
            .setDataType("arbitrary_data_type")
            .addTimerStat(KEY_1, TIMER_STAT_1)
            .addTimerStat(KEY_2, TIMER_STAT_2)
            .addMeasurement(KEY_1, MEASUREMENT_1)
            .addMeasurement(KEY_2, MEASUREMENT_2)
            .addStats(KEY_1, STATS_MAP)
            .addTimers(KEY_1, TIMERS_MAP)
            .addMeasurements(KEY_1, MEASUREMENTS_MAP)
            .build();

    assertThat(stats.getDataType()).isEqualTo("arbitrary_data_type");

    assertThat(stats.getTimerKeyCount()).isEqualTo(2);
    assertThat(stats.hasTimer(KEY_1)).isTrue();
    assertThat(stats.getTimerCount(KEY_1)).isEqualTo(TIMER_STAT_1.getCount());
    assertThat(stats.getTimerTime(KEY_1)).isEqualTo(TIMER_STAT_1.getTime());
    compareTimers(stats.getTimer(KEY_1), TIMER_STAT_1);
    assertThat(stats.hasTimer(KEY_2)).isTrue();
    assertThat(stats.getTimerCount(KEY_2)).isEqualTo(TIMER_STAT_2.getCount());
    assertThat(stats.getTimerTime(KEY_2)).isEqualTo(TIMER_STAT_2.getTime());
    compareTimers(stats.getTimer(KEY_2), TIMER_STAT_2);

    assertThat(stats.getMeasurementKeyCount()).isEqualTo(2);
    assertThat(stats.hasMeasurement(KEY_1)).isTrue();
    assertThat(stats.getMeasurement(KEY_1)).isEqualTo(MEASUREMENT_1);
    assertThat(stats.hasMeasurement(KEY_2)).isTrue();
    assertThat(stats.getMeasurement(KEY_2)).isEqualTo(MEASUREMENT_2);

    assertThat(stats.getMeasurementsKeyCount()).isEqualTo(1);
    assertThat(stats.hasMeasurements(KEY_1)).isTrue();
    assertThat(stats.getMeasurements(KEY_1)).isEqualTo(MEASUREMENTS_MAP);

    assertThat(stats.getStatsKeyCount()).isEqualTo(1);
    assertThat(stats.hasStats(KEY_1)).isTrue();
    assertThat(stats.getStats(KEY_1)).isEqualTo(STATS_MAP);

    assertThat(stats.getTimersKeyCount()).isEqualTo(1);
    assertThat(stats.hasTimers(KEY_1)).isTrue();
    assertThat(stats.getTimers(KEY_1)).isEqualTo(TIMERS_MAP);
  }

  @Test
  public void healthStats_keysAreIterable() {
    HealthStats stats =
        HealthStatsBuilder.newBuilder()
            .setDataType("arbitrary_data_type")
            .addMeasurement(KEY_1, MEASUREMENT_1)
            .addMeasurement(KEY_2, MEASUREMENT_2)
            .build();

    assertThat(stats.getMeasurementKeyCount()).isEqualTo(2);
    for (int i = 0; i < stats.getMeasurementKeyCount(); i++) {
      int key = stats.getMeasurementKeyAt(i);
      switch (key) {
        case KEY_1:
          assertThat(stats.getMeasurement(key)).isEqualTo(MEASUREMENT_1);
          break;
        case KEY_2:
          assertThat(stats.getMeasurement(key)).isEqualTo(MEASUREMENT_2);
          break;
        default:
          throw new IllegalStateException("Unexpected HealthStats key");
      }
    }
  }

  @Test
  public void toSortedIntArray_resultIsSorted() {
    HashSet<Integer> set = new HashSet<>();
    set.add(1);
    set.add(2);
    set.add(3);
    ReverseIteratingSet reversedSet = new ReverseIteratingSet(set);

    int[] setAsSortedArray = HealthStatsBuilder.toSortedIntArray(set);
    int[] reversedSetAsSortedArray = HealthStatsBuilder.toSortedIntArray(reversedSet);

    assertThat(Ints.asList(setAsSortedArray)).isInStrictOrder();
    assertThat(Ints.asList(reversedSetAsSortedArray)).isInStrictOrder();
  }

  private final void compareTimers(TimerStat timer1, TimerStat timer2) {
    assertThat(timer1.getCount()).isEqualTo(timer2.getCount());
    assertThat(timer1.getTime()).isEqualTo(timer2.getTime());
  }

  // Identical to HashSet<Integer>, except that the result of toArray() is reversed.
  private static final class ReverseIteratingSet extends HashSet<Integer> {
    public ReverseIteratingSet(HashSet<Integer> c) {
      super(c);
    }

    @Override
    public Object[] toArray() {
      Object[] forward = super.toArray();
      Object[] backward = new Object[forward.length];
      for (int i = 0; i < forward.length; i++) {
        backward[i] = forward[forward.length - 1 - i];
      }
      return backward;
    }
  }
}
