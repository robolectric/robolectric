package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;

import android.security.FileIntegrityManager;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow for {@link FileIntegrityManager}. */
@Implements(value = FileIntegrityManager.class, minSdk = R, isInAndroidSdk = false)
public class ShadowFileIntegrityManager {

  private boolean isApkVeritySupported = true;

  /** Sets the value of {@link #isApkVeritySupported}. */
  public void setIsApkVeritySupported(boolean isApkVeritySupported) {
    this.isApkVeritySupported = isApkVeritySupported;
  }

  /**
   * Returns {@code true} by default, or can be changed by {@link
   * #setIsApkVeritySupported(boolean)}.
   */
  @Implementation
  protected boolean isApkVeritySupported() {
    return isApkVeritySupported;
  }
}
