package org.robolectric.shadows;

import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import android.content.pm.PackageParser;
import android.content.pm.PackageParser.Package;
import android.os.Build;
import android.util.DisplayMetrics;
import java.io.File;
import java.util.List;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.res.FsFile;
import org.robolectric.shadows.ShadowLog.LogItem;
import org.robolectric.util.ReflectionHelpers;

@Implements(value = PackageParser.class, isInAndroidSdk = false)
public class ShadowPackageParser {

  @RealObject private PackageParser realObject;

  @Implementation
  protected void __constructor__() {
    realObject.setCallback(new Callback());
  }

  /** Parses an AndroidManifest.xml file using the framework PackageParser. */
  public static Package callParsePackage(FsFile apkFile) {
    PackageParser packageParser = new PackageParser();

    int flags = PackageParser.PARSE_IGNORE_PROCESSES;
    try {
      Package thePackage;
      if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.LOLLIPOP) {
        thePackage = packageParser.parsePackage(new File(apkFile.getPath()), flags);
      } else { // JB -> KK
        thePackage = ReflectionHelpers.callInstanceMethod(
            PackageParser.class,
            packageParser,
            "parsePackage",
            from(File.class, new File(apkFile.getPath())),
            from(String.class, apkFile.getPath()),
            from(DisplayMetrics.class, new DisplayMetrics()),
            from(int.class, flags));
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

  private static class Callback implements PackageParser.Callback {

    @Override
    public boolean hasFeature(String feature) {
      return false;
    }

    @Override
    public String[] getOverlayPaths(String targetPackageName, String targetPath) {
      return null;
    }

    @Override
    public String[] getOverlayApks(String targetPackageName) {
      return null;
    }
  }
}
