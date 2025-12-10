package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;

import android.util.StatsEvent;
import android.util.StatsLog;
import com.google.auto.value.AutoValue;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/** Shadow for {@link StatsLog} */
@Implements(value = StatsLog.class, minSdk = R)
public class ShadowStatsLog {

  @Implementation
  protected static void __staticInitializer__() {}

  private static final CopyOnWriteArrayList<StatsEvent> statsEvents = new CopyOnWriteArrayList<>();

  public static List<StatsLogItem> getStatsLogs() {
    return Lists.transform(
        statsEvents,
        statsEvent ->
            StatsLogItem.create(
                statsEvent.getAtomId(), statsEvent.getNumBytes(), statsEvent.getBytes()));
  }

  public static List<StatsEvent> getStatsEvents() {
    return Collections.unmodifiableList(statsEvents);
  }

  @Resetter
  public static void reset() {
    statsEvents.clear();
  }

  @Implementation
  public static void write(final StatsEvent statsEvent) {
    statsEvents.add(statsEvent);
  }

  /** Single atom log item for write api. */
  @AutoValue
  public abstract static class StatsLogItem {
    public abstract int atomId();

    public abstract int numBytes();

    @SuppressWarnings("AutoValueImmutableFields")
    public abstract byte[] bytes();

    public static StatsLogItem create(int atomId, int numBytes, byte[] bytes) {
      return new AutoValue_ShadowStatsLog_StatsLogItem(atomId, numBytes, bytes.clone());
    }
  }
}
