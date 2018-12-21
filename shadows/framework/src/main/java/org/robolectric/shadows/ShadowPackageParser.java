package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.pm.PackageParser;
import android.content.pm.PackageParser.Package;
import android.os.Build;
import android.util.DisplayMetrics;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implements;
import org.robolectric.res.Fs;
import org.robolectric.shadows.ShadowLog.LogItem;
import org.robolectric.util.reflector.ForType;

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
        thePackage = reflector(_PackageParser_.class, packageParser)
            .parsePackage(apkFile.toFile(), Fs.externalize(apkFile), new DisplayMetrics(), flags);
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

  @ForType(PackageParser.class)
  interface _PackageParser_ {

    Package parsePackage(File file, String fileName, DisplayMetrics displayMetrics, int flags);
  }
}
