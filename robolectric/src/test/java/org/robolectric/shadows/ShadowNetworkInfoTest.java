package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.net.NetworkInfo;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadow.api.Shadow;

@RunWith(AndroidJUnit4.class)
public class ShadowNetworkInfoTest {

  @Test
  public void getDetailedState_shouldReturnTheAssignedState() throws Exception {
    NetworkInfo networkInfo = Shadow.newInstanceOf(NetworkInfo.class);
    shadowOf(networkInfo).setDetailedState(NetworkInfo.DetailedState.SCANNING);
    assertThat(networkInfo.getDetailedState()).isEqualTo(NetworkInfo.DetailedState.SCANNING);
  }
}
