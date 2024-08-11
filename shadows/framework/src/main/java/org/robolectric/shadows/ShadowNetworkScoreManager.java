package org.robolectric.shadows;

import android.net.NetworkScoreManager;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/** Provides testing APIs for {@link NetworkScoreManager}. */
@Implements(value = NetworkScoreManager.class, isInAndroidSdk = false)
public class ShadowNetworkScoreManager {
  private static String activeScorerPackage;
  private static boolean isScoringEnabled = true;

  @Resetter
  public static void reset() {
    activeScorerPackage = null;
    isScoringEnabled = true;
  }

  @Implementation
  public String getActiveScorerPackage() {
    return activeScorerPackage;
  }

  @Implementation
  public boolean setActiveScorer(String packageName) {
    activeScorerPackage = packageName;
    return true;
  }

  /**
   * @see #isScoringEnabled()
   */
  @Implementation
  protected void disableScoring() {
    isScoringEnabled = false;
  }

  /** Whether scoring is enabled. */
  public boolean isScoringEnabled() {
    return isScoringEnabled;
  }
}
