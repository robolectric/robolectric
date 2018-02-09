package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;

import android.content.RestrictionsManager;
import android.os.Bundle;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow of {@link android.content.RestrictionsManager}. */
@Implements(value = RestrictionsManager.class, minSdk=LOLLIPOP)
public class ShadowRestrictionsManager {
  private Bundle applicationRestrictions;

  /**
   * Sets the application restrictions as returned by {@link RestrictionsManager#getApplicationRestrictions()}.
   */
  public void setApplicationRestrictions(Bundle applicationRestrictions) {
    this.applicationRestrictions = applicationRestrictions;
  }

  /**
   * @return `null` by default, or the value specified via {@link #setApplicationRestrictions(Bundle)}
   */
  @Implementation
  protected Bundle getApplicationRestrictions() {
    return applicationRestrictions;
  }
}
