package org.robolectric.shadows;

import android.net.NetworkScoreManager;
import android.os.Build;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Provides testing APIs for {@link NetworkScoreManager}. */
@Implements(
  value = NetworkScoreManager.class,
  isInAndroidSdk = false,
  minSdk = Build.VERSION_CODES.LOLLIPOP
)
public class ShadowNetworkScoreManager {
  private String activeScorerPackage;
  private boolean isScoringEnabled = true;

  @Implementation
  public String getActiveScorerPackage() {
    return activeScorerPackage;
  }

  @Implementation
  public boolean setActiveScorer(String packageName) {
    activeScorerPackage = packageName;
    return true;
  }

  /** @see #isScoringEnabled() */
  @Implementation
  protected void disableScoring() {
    isScoringEnabled = false;
  }

  /** Whether scoring is enabled. */
  public boolean isScoringEnabled() {
    return isScoringEnabled;
  }
}
