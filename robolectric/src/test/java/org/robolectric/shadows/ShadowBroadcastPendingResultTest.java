package org.robolectric.shadows;

import android.os.Bundle;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowBroadcastPendingResultTest {
  @Test
  public void testCreate() throws Exception {
    assertThat(ShadowBroadcastPendingResult.create(1, "result", new Bundle(), true))
        .isNotNull();
  }
}