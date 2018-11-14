package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.os.Bundle;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowBroadcastPendingResultTest {
  @Test
  public void testCreate() throws Exception {
    assertThat(ShadowBroadcastPendingResult.create(1, "result", new Bundle(), true))
        .isNotNull();
  }
}