package org.robolectric.shadows;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ShadowGooglePlayServicesUtilTest {

  @Test
  public void isGooglePlayServicesAvailable_shouldGetSet() {
    assertThat(GooglePlayServicesUtil.isGooglePlayServicesAvailable(RuntimeEnvironment.application)).isEqualTo(ConnectionResult.SERVICE_MISSING);
    ShadowGooglePlayServicesUtil.setIsGooglePlayServicesAvailable(ConnectionResult.SUCCESS);
    assertThat(GooglePlayServicesUtil.isGooglePlayServicesAvailable(RuntimeEnvironment.application)).isEqualTo(ConnectionResult.SUCCESS);
  }
}
