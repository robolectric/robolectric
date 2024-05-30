package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.net.NetworkScoreManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadow.api.Shadow;

/** ShadowNetworkScoreManagerTest tests {@link ShadowNetworkScoreManager}. */
@RunWith(AndroidJUnit4.class)
public final class ShadowNetworkScoreManagerTest {

  @Test
  public void testGetActiveScorerPackage() {
    Context context = ApplicationProvider.getApplicationContext();
    NetworkScoreManager networkScoreManager =
        (NetworkScoreManager) context.getSystemService(Context.NETWORK_SCORE_SERVICE);

    String testPackage = "com.package.test";
    networkScoreManager.setActiveScorer(testPackage);
    assertThat(networkScoreManager.getActiveScorerPackage()).isEqualTo(testPackage);
  }

  @Test
  public void testIsScoringEnabled() {
    Context context = ApplicationProvider.getApplicationContext();
    NetworkScoreManager networkScoreManager =
        (NetworkScoreManager) context.getSystemService(Context.NETWORK_SCORE_SERVICE);
    networkScoreManager.disableScoring();
    ShadowNetworkScoreManager m = Shadow.extract(networkScoreManager);
    assertThat(m.isScoringEnabled()).isFalse();
  }
}
