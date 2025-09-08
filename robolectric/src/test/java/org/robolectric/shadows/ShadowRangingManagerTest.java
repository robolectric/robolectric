package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.BAKLAVA;
import static android.ranging.RangingCapabilities.ENABLED;
import static android.ranging.RangingManager.UWB;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robolectric.RuntimeEnvironment.getApplication;

import android.ranging.RangingCapabilities;
import android.ranging.RangingManager;
import android.ranging.RangingManager.RangingCapabilitiesCallback;
import com.google.common.util.concurrent.MoreExecutors;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = BAKLAVA)
public final class ShadowRangingManagerTest {

  @Rule public final MockitoRule mockito = MockitoJUnit.rule();

  @Mock RangingCapabilitiesCallback rangingCapabilitiesCallback;

  private static final RangingCapabilities RANGING_CAPABILITIES =
      new RangingCapabilitiesBuilder().addAvailability(UWB, ENABLED).build();

  @Test
  public void setCapabilities_capabilitiesReportedAfterRegisteringCallback() throws Exception {
    RangingManager rangingManager = getApplication().getSystemService(RangingManager.class);
    ShadowRangingManager shadowRangingManager = Shadow.extract(rangingManager);

    shadowRangingManager.updateCapabilities(RANGING_CAPABILITIES);
    rangingManager.registerCapabilitiesCallback(
        MoreExecutors.directExecutor(), rangingCapabilitiesCallback);

    verify(rangingCapabilitiesCallback).onRangingCapabilities(RANGING_CAPABILITIES);
  }

  @Test
  public void triggerCapabilitiesCallback_capabilitiesCallbackTriggeredTwice() throws Exception {
    RangingManager rangingManager = getApplication().getSystemService(RangingManager.class);
    ShadowRangingManager shadowRangingManager = Shadow.extract(rangingManager);

    shadowRangingManager.updateCapabilities(RANGING_CAPABILITIES);
    rangingManager.registerCapabilitiesCallback(
        MoreExecutors.directExecutor(), rangingCapabilitiesCallback);
    shadowRangingManager.updateCapabilities(RANGING_CAPABILITIES);

    verify(rangingCapabilitiesCallback, times(2)).onRangingCapabilities(RANGING_CAPABILITIES);
  }
}
