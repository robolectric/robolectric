package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.pm.PackageInfo;
import android.content.pm.PackageParser;
import android.content.pm.PackageParser.Package;
import android.content.pm.PackageUserState;
import android.os.Build;
import android.util.ArraySet;
import android.util.DisplayMetrics;
import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implements;
import org.robolectric.res.Fs;
import org.robolectric.shadows.ShadowLog.LogItem;
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

    int flags = PackageParser.PARSE_IGNORE_PROCESSES;
    try {
      Package thePackage;
      if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.LOLLIPOP) {
        thePackage = packageParser.parsePackage(apkFile.toFile(), flags);
      } else { // JB -> KK
        thePackage =
            reflector(_PackageParser_.class, packageParser)
                .parsePackage(
                    apkFile.toFile(), Fs.externalize(apkFile), new DisplayMetrics(), flags);
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
        return generatePackageInfo(p, gids, flags, firstInstallTime, lastUpdateTime,
            new HashSet<>(), new PackageUserState());
      } else if (apiLevel <= LOLLIPOP_MR1) {
        return generatePackageInfo(p, gids, flags, firstInstallTime, lastUpdateTime,
            new ArraySet<>(), new PackageUserState());
      } else {
        return PackageParser.generatePackageInfo(p, gids, flags, firstInstallTime, lastUpdateTime,
            new HashSet<>(), new PackageUserState());
      }
    }

    Package parsePackage(File file, String fileName, DisplayMetrics displayMetrics, int flags);
  }

  /** Accessor interface for {@link Package}'s internals. */
  @ForType(Package.class)
  public interface _Package_ {

    @Accessor("mPath")
    String getPath();
  }
}
