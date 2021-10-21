package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;

import com.android.server.pm.parsing.library.PackageSharedLibraryUpdater;
import java.util.List;
import java.util.function.Supplier;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Shadow of {@link PackageBackwardCompatibility} to handle a scenario that can come up when
 * multiple Android versions end up on the classpath
 */
@Implements(className = "android.content.pm.PackageBackwardCompatibility", maxSdk = P)
public class ShadowPackageBackwardCompatibility {

  /**
   * Stubbing this out as if Android S+ is on the classpath, we'll get a ClassCastException instead
   * of a ClassNotFoundException. Since we don't really need this logic, simpler to just skip it
   */
  @Implementation
  protected static boolean addOptionalUpdater(
      List<PackageSharedLibraryUpdater> packageUpdaters,
      String className,
      Supplier<PackageSharedLibraryUpdater> defaultUpdater) {
    return false;
  }
}
