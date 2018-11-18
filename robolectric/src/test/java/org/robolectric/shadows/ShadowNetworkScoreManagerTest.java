package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.net.NetworkScoreManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** ShadowNetworkScoreManagerTest tests {@link ShadowNetworkScoreManager}. */
@RunWith(AndroidJUnit4.class)
public final class ShadowNetworkScoreManagerTest {

  @Test
  @Config(minSdk = LOLLIPOP)
  public void testGetActiveScorerPackage() throws Exception {
    Context context = ApplicationProvider.getApplicationContext();
    NetworkScoreManager networkScoreManager =
        (NetworkScoreManager) context.getSystemService(Context.NETWORK_SCORE_SERVICE);

    String testPackage = "com.package.test";
    networkScoreManager.setActiveScorer(testPackage);
    assertThat(networkScoreManager.getActiveScorerPackage()).isEqualTo(testPackage);
  }
}
