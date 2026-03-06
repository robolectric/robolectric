package org.robolectric.shadows;

import android.content.pm.PackageInfo;
import android.content.pm.PackageParser;
import android.content.pm.PackageParser.Callback;
import android.content.pm.PackageParser.Package;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implements;
import org.robolectric.shadows.ShadowLog.LogItem;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;
import org.robolectric.util.reflector.WithType;

@Implements(value = PackageParser.class, isInAndroidSdk = false)
@SuppressWarnings("NewApi")
public class ShadowPackageParser {

  /** Parses an AndroidManifest.xml file using the framework PackageParser. */
  public static Package callParsePackage(Path apkFile) {
    PackageParser packageParser = new PackageParser();

    // The deprecated PackageParser emits warnings for manifest elements it doesn't understand,
    // such as <queries> (API 30) and <property> under <service> (API 33). These are valid
    // manifest elements - the warnings are an artifact of using the deprecated parser.
    // Suppress them to avoid spamming logs on every test class initialization.
    // Note: this level intentionally persists for the duration of the test class.
    // ShadowLog.reset() (called by @Resetter between test classes) restores the default.
    ShadowLog.setLoggable("PackageParser", Log.ERROR);

    try {
      // TODO(christianw/brettchabot): workaround for NPE from probable bug in Q.
      // Can be removed when upstream properly handles a null callback
      // PackageParser#setMinAspectRatio(Package)
      if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.Q) {
        QHelper.setCallback(packageParser);
      }
      Package thePackage = packageParser.parsePackage(apkFile.toFile(), 0);

      if (thePackage == null) {
        List<LogItem> logItems = ShadowLog.getLogsForTag("PackageParser");
        if (logItems.isEmpty()) {
          throw new RuntimeException("Failed to parse package " + apkFile);
        } else {
          // Prefer the first error-level log; fall back to the first entry if none found,
          // since warnings (e.g. "Unknown element") may precede the actual parse failure.
          LogItem logItem =
              logItems.stream()
                  .filter(i -> i.type >= Log.ERROR)
                  .findFirst()
                  .orElse(logItems.get(0));
          throw new RuntimeException(
              "Failed to parse package " + apkFile + ": " + logItem.msg, logItem.throwable);
        }
      }

      return thePackage;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /** Prevents ClassNotFoundError for Callback on pre-26. */
  private static class QHelper {
    private static void setCallback(PackageParser packageParser) {
      // TODO(christianw): this should be a CallbackImpl with the ApplicationPackageManager...

      packageParser.setCallback(
          new Callback() {
            @Override
            public boolean hasFeature(String s) {
              return false;
            }

            // @Override for SDK < 30
            public String[] getOverlayPaths(String s, String s1) {
              return null;
            }

            // @Override for SDK < 30
            public String[] getOverlayApks(String s) {
              return null;
            }
          });
    }
  }

  /** Accessor interface for {@link PackageParser}'s internals. */
  @ForType(PackageParser.class)
  interface PackageParserReflector {

    @Static
    PackageInfo generatePackageInfo(
        PackageParser.Package p,
        int[] gids,
        int flags,
        long firstInstallTime,
        long lastUpdateTime,
        Set<String> grantedPermissions,
        @WithType("android.content.pm.PackageUserState") Object state);

    default PackageInfo generatePackageInfo(
        PackageParser.Package p,
        int[] gids,
        int flags,
        long firstInstallTime,
        long lastUpdateTime) {

      return generatePackageInfo(
          p,
          gids,
          flags,
          firstInstallTime,
          lastUpdateTime,
          (Set<String>) new HashSet<String>(),
          newPackageUserState());
    }

    Package parsePackage(File file, String fileName, DisplayMetrics displayMetrics, int flags);
  }

  private static Object newPackageUserState() {
    try {
      return ReflectionHelpers.newInstance(Class.forName("android.content.pm.PackageUserState"));
    } catch (ClassNotFoundException e) {
      throw new AssertionError(e);
    }
  }

  /** Accessor interface for {@link Package}'s internals. */
  @ForType(Package.class)
  public interface PackageReflector {

    @Accessor("mPath")
    String getPath();
  }
}
