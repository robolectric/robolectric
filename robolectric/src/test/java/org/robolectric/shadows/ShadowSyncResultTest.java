package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.SyncResult;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowSyncResultTest {

  @Test
  public void testConstructor() throws Exception {
    SyncResult result = new SyncResult();
    assertThat(result.stats).isNotNull();
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
    assertThat(result.stats.numInserts).isEqualTo(0L);
  }
}
