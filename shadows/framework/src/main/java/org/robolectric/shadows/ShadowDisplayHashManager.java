package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.S;

import android.view.displayhash.DisplayHash;
import android.view.displayhash.DisplayHashManager;
import android.view.displayhash.VerifiedDisplayHash;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.util.Collection;
import java.util.Set;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Shadow of {@link android.view.displayhash.DisplayHashManager}. */
@Implements(value = DisplayHashManager.class, isInAndroidSdk = false, minSdk = S)
public class ShadowDisplayHashManager {

  private static VerifiedDisplayHash verifyDisplayHashResult;
  private static Set<String> supportedHashAlgorithms = ImmutableSet.of("PHASH");

  /**
   * Sets the {@link VerifiedDisplayHash} that's going to be returned by following
   * {DisplayHashManager#verifyDisplayHash} calls.
   */
  public static void setVerifyDisplayHashResult(VerifiedDisplayHash verifyDisplayHashResult) {
    ShadowDisplayHashManager.verifyDisplayHashResult = verifyDisplayHashResult;
  }

  /**
   * Sets the return value of #getSupportedHashAlgorithms.
   *
   * <p>If null is provided, getSupportedHashAlgorithms will throw a RuntimeException.
   */
  public static void setSupportedHashAlgorithms(Collection<String> supportedHashAlgorithms) {
    if (supportedHashAlgorithms == null) {
      ShadowDisplayHashManager.supportedHashAlgorithms = null;
    } else {
      ShadowDisplayHashManager.supportedHashAlgorithms =
          ImmutableSet.copyOf(supportedHashAlgorithms);
    }
  }

  @Implementation(minSdk = S)
  protected Set<String> getSupportedHashAlgorithms() {
    return Preconditions.checkNotNull(supportedHashAlgorithms);
  }

  @Implementation(minSdk = S)
  protected VerifiedDisplayHash verifyDisplayHash(DisplayHash displayHash) {
    return verifyDisplayHashResult;
  }
}
