package org.robolectric.shadows;

import android.net.NetworkInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import org.robolectric.internal.Shadow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowNetworkInfoTest {

  @Test
  public void getDetailedState_shouldReturnTheAssignedState() throws Exception {
    NetworkInfo networkInfo = Shadow.newInstanceOf(NetworkInfo.class);
    shadowOf(networkInfo).setDetailedState(NetworkInfo.DetailedState.SCANNING);
    assertThat(networkInfo.getDetailedState()).isEqualTo(NetworkInfo.DetailedState.SCANNING);
  }
}
