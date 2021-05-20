package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;

import android.util.StatsEvent;
import android.util.StatsLog;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/**
 *
 */
@Implements(value = StatsLog.class, minSdk = R)
public class ShadowStatsLog {

  public static final byte TYPE_INT = 0x00;
  public static final byte TYPE_LONG = 0x01;
  public static final byte TYPE_OBJECT = 0x07;

  private static CopyOnWriteArrayList<StatsLogItem> statsLogs =
      new CopyOnWriteArrayList<>();

  public static List<StatsLogItem> getStatsLogs() {
    return Collections.unmodifiableList(statsLogs);
  }

  @Resetter
  public static void reset() {
    statsLogs = new CopyOnWriteArrayList<>();
  }

  @Implementation
  public static void write(final StatsEvent statsEvent) {
    statsLogs.add(new StatsLogItem(statsEvent.getAtomId(), statsEvent.getNumBytes(),
                      statsEvent.getBytes()));
  }

  /**
   * Single atom log item for write api.
   */
  public static final class StatsLogItem {
    public final int atomId;
    public final int numBytes;
    public final byte[] bytes;

    public StatsLogItem(int atomId, int numBytes, byte[] bytes) {
      this.atomId = atomId;
      this.numBytes = numBytes;
      this.bytes = bytes;
    }
  }
}
