package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static org.assertj.core.api.Assertions.assertThat;

import android.content.Context;
import android.net.NetworkScoreManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/** ShadowNetworkScoreManagerTest tests {@link ShadowNetworkScoreManager}. */
@RunWith(RobolectricTestRunner.class)
public final class ShadowNetworkScoreManagerTest {

  @Test
  @Config(minSdk = LOLLIPOP)
  public void testGetActiveScorerPackage() throws Exception {
    NetworkScoreManager networkScoreManager =
        (NetworkScoreManager)
            RuntimeEnvironment.application.getSystemService(Context.NETWORK_SCORE_SERVICE);

    String testPackage = "com.package.test";
    networkScoreManager.setActiveScorer(testPackage);
    assertThat(networkScoreManager.getActiveScorerPackage()).isEqualTo(testPackage);
  }
}
