package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.P;

import java.util.List;
import java.util.function.Supplier;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Shadow of {@link android.content.pm.PackageBackwardCompatibility} to handle a scenario that can
 * come up when multiple Android versions end up on the classpath.
 */
@Implements(
    className = "android.content.pm.PackageBackwardCompatibility",
    minSdk = O_MR1,
    maxSdk = P)
public class ShadowPackageBackwardCompatibility {

  /**
   * Stubbing this out as if Android S+ is on the classpath, we'll get a ClassCastException instead
   * of a ClassNotFoundException. Since we don't really need this logic, simpler to just skip it
   */
  @Implementation(minSdk = P)
  protected static boolean addOptionalUpdater(
      List<Object> packageUpdaters, String className, Supplier<Object> defaultUpdater) {
    return false;
  }
}
