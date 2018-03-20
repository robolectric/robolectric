package org.robolectric.shadows;

import static android.content.pm.PackageManager.INSTALL_PARSE_FAILED_NOT_APK;
import static android.content.pm.PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION;
import static android.content.pm.PackageParser.PARSE_COLLECT_CERTIFICATES;
import static org.robolectric.shadow.api.Shadow.directlyOn;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.PackageParser.Package;
import android.content.pm.Signature;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Slog;
import android.util.TypedValue;
import com.android.internal.util.XmlUtils;
import java.io.File;
import java.io.IOException;
import java.security.cert.Certificate;
import libcore.io.IoUtils;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.res.FsFile;
import org.robolectric.util.ReflectionHelpers;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Shadow for {@link PackageParser}
 */
@Implements(value = PackageParser.class, isInAndroidSdk = false)
public class ShadowPackageParser {

  /** Parses an AndroidManifest.xml file using the framework PackageParser. */
  public static Package callParsePackage(FsFile manifestFile) {
    PackageParser packageParser = new PackageParser();

    int flags = PackageParser.PARSE_IGNORE_PROCESSES;
    try {
      if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.LOLLIPOP) {
        return packageParser.parsePackage(new File(manifestFile.getPath()), flags);
      } else { // JB -> KK
        return ReflectionHelpers.callInstanceMethod(
            PackageParser.class,
            packageParser,
            "parsePackage",
            from(File.class, new File(manifestFile.getPath())),
            from(String.class, manifestFile.getPath()),
            from(DisplayMetrics.class, new DisplayMetrics()),
            from(int.class, flags));
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
