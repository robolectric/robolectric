package org.robolectric.shadows;

import android.content.SyncStats;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(SyncStats.class)
public class ShadowSyncStats {
  @RealObject
  private SyncStats stats;

  @Implementation
  public void clear() {
    stats.numAuthExceptions = 0;
    stats.numIoExceptions = 0;
    stats.numParseExceptions = 0;
    stats.numConflictDetectedExceptions = 0;
    stats.numInserts = 0;
    stats.numUpdates = 0;
    stats.numDeletes = 0;
    stats.numEntries = 0;
    stats.numSkippedEntries = 0;
  }
}
