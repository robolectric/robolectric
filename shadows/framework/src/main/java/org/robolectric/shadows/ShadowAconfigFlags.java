package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.VANILLA_ICE_CREAM;

import com.android.internal.pm.pkg.component.AconfigFlags;
import java.util.Collections;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.util.ReflectionHelpers;

/**
 * Shadow for {@code com.android.internal.pm.pkg.component.AconfigFlags}, which package parsing uses
 * to evaluate {@code android:featureFlag} in manifests.
 *
 * <p>On V its constructor reads the device's aconfig flag files, none of which exist under
 * Robolectric, and logs an error with a stack trace for each one. This shadow leaves the list of
 * files empty so the real constructor finds nothing to read, as later SDKs do for missing files,
 * and as {@link ShadowAconfigPackage} does for flag storage.
 */
@Implements(
    className = "com.android.internal.pm.pkg.component.AconfigFlags",
    isInAndroidSdk = false,
    minSdk = VANILLA_ICE_CREAM,
    maxSdk = VANILLA_ICE_CREAM)
public class ShadowAconfigFlags {

  /** Leaves the list of the device's flag files empty. Setting it is all the initializer does. */
  @Implementation
  protected static void __staticInitializer__() {
    ReflectionHelpers.setStaticField(
        AconfigFlags.class, "sTextProtoFilesOnDevice", Collections.emptyList());
  }
}
