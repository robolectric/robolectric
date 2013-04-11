package org.robolectric.shadows;

import android.content.SyncStats;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.robolectric.internal.RealObject;

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
