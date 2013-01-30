package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

import android.content.SyncResult;
import android.content.SyncStats;

import java.lang.reflect.Field;

@Implements(SyncResult.class)
public class ShadowSyncResult {
    @RealObject
    private SyncResult result;

    public void __constructor__() {
        try {
            Field f = SyncResult.class.getDeclaredField("stats");
            f.setAccessible(true);
            f.set(result, new SyncStats());
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Implementation
    public boolean hasSoftError() {
        return result.syncAlreadyInProgress || result.stats.numIoExceptions > 0;
    }

    @Implementation
    public boolean hasHardError() {
        return result.stats.numParseExceptions > 0
                || result.stats.numConflictDetectedExceptions > 0
                || result.stats.numAuthExceptions > 0
                || result.tooManyDeletions
                || result.tooManyRetries
                || result.databaseError;
    }

    @Implementation
    public boolean hasError() {
        return hasSoftError() || hasHardError();
    }

    @Implementation
    public boolean madeSomeProgress() {
        return ((result.stats.numDeletes > 0) && !result.tooManyDeletions)
                || result.stats.numInserts > 0
                || result.stats.numUpdates > 0;
    }

    @Implementation
    public void clear() {
        if (result.syncAlreadyInProgress) {
            throw new UnsupportedOperationException(
                    "you are not allowed to clear the ALREADY_IN_PROGRESS SyncStats");
        }
        result.tooManyDeletions = false;
        result.tooManyRetries = false;
        result.databaseError = false;
        result.fullSyncRequested = false;
        result.partialSyncUnavailable = false;
        result.moreRecordsToGet = false;
        result.delayUntil = 0;
        result.stats.clear();
    }


    @Implements(SyncStats.class)
    public static class ShadowSyncStats {
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
}
