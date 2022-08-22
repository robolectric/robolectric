package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;

import android.util.StatsEvent;
import android.util.StatsLog;
import com.google.auto.value.AutoValue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/** Shadow for {@link StatsLog} */
@Implements(value = StatsLog.class, minSdk = R)
public class ShadowStatsLog {

  @Implementation
  protected static void __staticInitializer__() {}

  private static List<StatsLogItem> statsLogs = Collections.synchronizedList(new ArrayList<>());

  public static List<StatsLogItem> getStatsLogs() {
    return Collections.unmodifiableList(statsLogs);
  }

  @Resetter
  public static void reset() {
    statsLogs = Collections.synchronizedList(new ArrayList<>());
  }

  @Implementation
  public static void write(final StatsEvent statsEvent) {
    statsLogs.add(
        StatsLogItem.create(
            statsEvent.getAtomId(), statsEvent.getNumBytes(), statsEvent.getBytes()));
    statsEvent.release();
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
