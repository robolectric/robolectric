package com.xtremelabs.robolectric.shadows;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.SyncResult;


@RunWith(WithTestDefaultsRunner.class)
public class SyncResultTest {

    @Test
    public void testConstructor() throws Exception {
        SyncResult result = new SyncResult();
        assertThat(result.stats, not(nullValue()));
    }

    @Test
    public void hasSoftError() throws Exception {
        SyncResult result = new SyncResult();
        assertFalse(result.hasSoftError());
        result.stats.numIoExceptions++;
        assertTrue(result.hasSoftError());
        assertTrue(result.hasError());
    }

    @Test
    public void hasHardError() throws Exception {
        SyncResult result = new SyncResult();
        assertFalse(result.hasHardError());
        result.stats.numAuthExceptions++;
        assertTrue(result.hasHardError());
        assertTrue(result.hasError());
    }

    @Test
    public void testMadeSomeProgress() throws Exception {
        SyncResult result = new SyncResult();
        assertFalse(result.madeSomeProgress());
        result.stats.numInserts++;
        assertTrue(result.madeSomeProgress());
    }

    @Test
    public void testClear() throws Exception {
        SyncResult result = new SyncResult();
        result.moreRecordsToGet = true;
        result.stats.numInserts++;
        result.clear();
        assertFalse(result.moreRecordsToGet);
        assertThat(result.stats.numInserts, is(0L));
    }
}
