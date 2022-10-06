package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.pm.PackageInfo;
import android.content.pm.PackageParser;
import android.content.pm.PackageParser.Callback;
import android.content.pm.PackageParser.Package;
import android.os.Build;
import android.util.ArraySet;
import android.util.DisplayMetrics;
import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implements;
import org.robolectric.res.Fs;
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

    try {
      Package thePackage;
      if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.LOLLIPOP) {
        // TODO(christianw/brettchabot): workaround for NPE from probable bug in Q.
        // Can be removed when upstream properly handles a null callback
        // PackageParser#setMinAspectRatio(Package)
        if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.Q) {
          QHelper.setCallback(packageParser);
        }
        thePackage = packageParser.parsePackage(apkFile.toFile(), 0);
      } else { // JB -> KK
        thePackage =
            reflector(_PackageParser_.class, packageParser)
                .parsePackage(apkFile.toFile(), Fs.externalize(apkFile), new DisplayMetrics(), 0);
      }

      if (thePackage == null) {
        List<LogItem> logItems = ShadowLog.getLogsForTag("PackageParser");
        if (logItems.isEmpty()) {
          throw new RuntimeException(
              "Failed to parse package " + apkFile);
        } else {
          LogItem logItem = logItems.get(0);
          throw new RuntimeException(
              "Failed to parse package " + apkFile + ": " + logItem.msg, logItem.throwable);
        }
      }

      return thePackage;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Prevents ClassNotFoundError for Callback on pre-26.
   */
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
  interface _PackageParser_ {

    // <= JELLY_BEAN
    @Static
    PackageInfo generatePackageInfo(
        PackageParser.Package p,
        int[] gids,
        int flags,
        long firstInstallTime,
        long lastUpdateTime,
        HashSet<String> grantedPermissions);

    // <= LOLLIPOP
    @Static
    PackageInfo generatePackageInfo(
        PackageParser.Package p,
        int[] gids,
        int flags,
        long firstInstallTime,
        long lastUpdateTime,
        HashSet<String> grantedPermissions,
        @WithType("android.content.pm.PackageUserState")
            Object state);

    // LOLLIPOP_MR1
    @Static
    PackageInfo generatePackageInfo(
        PackageParser.Package p,
        int[] gids,
        int flags,
        long firstInstallTime,
        long lastUpdateTime,
        ArraySet<String> grantedPermissions,
        @WithType("android.content.pm.PackageUserState")
            Object state);

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
      int apiLevel = RuntimeEnvironment.getApiLevel();

      if (apiLevel <= JELLY_BEAN) {
        return generatePackageInfo(p, gids, flags, firstInstallTime, lastUpdateTime,
            new HashSet<>());
      } else if (apiLevel <= LOLLIPOP) {
        return generatePackageInfo(
            p,
            gids,
            flags,
            firstInstallTime,
            lastUpdateTime,
            new HashSet<>(),
            newPackageUserState());
      } else if (apiLevel <= LOLLIPOP_MR1) {
        return generatePackageInfo(
            p,
            gids,
            flags,
            firstInstallTime,
            lastUpdateTime,
            new ArraySet<>(),
            newPackageUserState());
      } else {
        return generatePackageInfo(
            p,
            gids,
            flags,
            firstInstallTime,
            lastUpdateTime,
            (Set<String>) new HashSet<String>(),
            newPackageUserState());
      }
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
  public interface _Package_ {

    @Accessor("mPath")
    String getPath();
  }
}
