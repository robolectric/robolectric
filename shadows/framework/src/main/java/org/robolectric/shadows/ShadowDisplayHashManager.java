package org.robolectric.shadows;

import android.view.displayhash.DisplayHash;
import android.view.displayhash.DisplayHashManager;
import android.view.displayhash.VerifiedDisplayHash;
import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow of {@link android.view.displayhash.DisplayHashManager}. */
@Implements(value = DisplayHashManager.class, isInAndroidSdk = false)
public class ShadowDisplayHashManager {

  private static VerifiedDisplayHash verifyDisplayHashResult;

  /**
   * Sets the {@link VerifiedDisplayHash} that's going to be returned by following
   * {DisplayHashManager#verifyDisplayHash} calls.
   */
  public static void setVerifyDisplayHashResult(VerifiedDisplayHash verifyDisplayHashResult) {
    ShadowDisplayHashManager.verifyDisplayHashResult = verifyDisplayHashResult;
  }

  @Implementation(minSdk = 31)
  protected Set<String> getSupportedHashAlgorithms() {
    return ImmutableSet.of("PHASH");
  }

  @Implementation(minSdk = 31)
  protected VerifiedDisplayHash verifyDisplayHash(DisplayHash displayHash) {
    return verifyDisplayHashResult;
  }
}
