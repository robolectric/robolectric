package org.robolectric.shadows;

import android.net.NetworkInfo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class NetworkInfoTest {

  @Test
  public void getDetailedState_shouldReturnTheAssignedState() throws Exception {
    NetworkInfo networkInfo = Robolectric.newInstanceOf(NetworkInfo.class);
    shadowOf(networkInfo).setDetailedState(NetworkInfo.DetailedState.SCANNING);
    assertThat(networkInfo.getDetailedState()).isEqualTo(NetworkInfo.DetailedState.SCANNING);
  }
}
