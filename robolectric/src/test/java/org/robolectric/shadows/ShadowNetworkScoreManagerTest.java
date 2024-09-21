package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.content.Context;
import android.net.NetworkScoreManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
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

  @Test
  @Config(minSdk = O)
  public void
      networkScoreManager_activityContextEnabled_differentInstancesRetrieveActiveScorerPackage() {
    String originalProperty = System.getProperty("robolectric.createActivityContexts", "");
    System.setProperty("robolectric.createActivityContexts", "true");
    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class).setup()) {
      NetworkScoreManager applicationNetworkScoreManager =
          RuntimeEnvironment.getApplication().getSystemService(NetworkScoreManager.class);
      Activity activity = controller.get();
      NetworkScoreManager activityNetworkScoreManager =
          activity.getSystemService(NetworkScoreManager.class);

      assertThat(applicationNetworkScoreManager).isNotSameInstanceAs(activityNetworkScoreManager);

      String applicationScorerPackage = applicationNetworkScoreManager.getActiveScorerPackage();
      String activityScorerPackage = activityNetworkScoreManager.getActiveScorerPackage();

      assertThat(activityScorerPackage).isEqualTo(applicationScorerPackage);
    } finally {
      System.setProperty("robolectric.createActivityContexts", originalProperty);
    }
  }
}
