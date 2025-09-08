package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.os.Build.VERSION_CODES;
import android.os.flagging.AconfigPackage;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;

/**
 * Shadow for AconfigPackage.
 *
 * <p>The real Android implementation reads from metadata files that do not exist in Robolectric.
 * This shadow just stubs out that implementation to avoid the performance hit and error logging
 * that can occur when using the real implementation.
 */
@Implements(value = AconfigPackage.class, minSdk = VERSION_CODES.BAKLAVA, isInAndroidSdk = false)
public class ShadowAconfigPackage {

  private static final AconfigPackage emptyAconfigPackage =
      reflector(AconfigPackageReflector.class).newAconfigPackage();

  @Implementation
  protected static AconfigPackage load(String packageName) {
    // just return an empty implementation
    return emptyAconfigPackage;
  }

  @Implementation
  protected boolean getBooleanFlagValue(String flagName, boolean defaultValue) {
    return defaultValue;
  }

  /**
   * AconfigPackageReflector is used to create an empty AconfigPackage.
   *
   * <p>This is necessary because AconfigPackage's constructor is private.
   */
  @ForType(AconfigPackage.class)
  protected interface AconfigPackageReflector {

    @Constructor
    AconfigPackage newAconfigPackage();
  }
}
