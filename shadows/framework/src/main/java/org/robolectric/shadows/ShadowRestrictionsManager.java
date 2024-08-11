package org.robolectric.shadows;

import android.content.RestrictionsManager;
import android.os.Bundle;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;

/** Shadow of {@link android.content.RestrictionsManager}. */
@Implements(value = RestrictionsManager.class)
public class ShadowRestrictionsManager {
  private static Bundle applicationRestrictions;

  @Resetter
  public static void reset() {
    applicationRestrictions = null;
  }

  /**
   * Sets the application restrictions as returned by {@link
   * RestrictionsManager#getApplicationRestrictions()}.
   */
  public void setApplicationRestrictions(Bundle applicationRestrictions) {
    this.applicationRestrictions = applicationRestrictions;
  }

  /**
   * @return null by default, or the value specified via {@link #setApplicationRestrictions(Bundle)}
   */
  @Implementation
  protected Bundle getApplicationRestrictions() {
    return applicationRestrictions;
  }
}
