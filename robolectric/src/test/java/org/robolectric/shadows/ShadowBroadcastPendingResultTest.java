package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.os.Bundle;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowBroadcastPendingResultTest {
  @Test
  public void testCreate() throws Exception {
    assertThat(ShadowBroadcastPendingResult.create(1, "result", new Bundle(), true))
        .isNotNull();
  }
}