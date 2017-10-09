package org.robolectric.shadows;

import static android.content.pm.PackageManager.*;
import static android.content.pm.PackageManager.MATCH_DIRECT_BOOT_AWARE;
import static android.content.pm.PackageManager.MATCH_DIRECT_BOOT_UNAWARE;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import android.content.pm.PackageInfo;
import android.content.pm.PackageUserState;
import android.os.Build;
import java.util.HashSet;
import org.robolectric.res.FsFile;
import org.robolectric.res.builder.XmlBlock;
import android.content.pm.PackageParser;
import android.content.pm.PackageParser.Package;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import libcore.io.IoUtils;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;

public class AndroidManifestPullParser {

  public Package parse(String packageName, FsFile manifestPath, Resources resources) {
    XmlResourceParser parser = null;
    try {

      XmlBlock xmlBlock = XmlBlock.create(manifestPath, packageName);
      parser = ShadowAssetManager.getXmlResourceParser(RuntimeEnvironment.getAppResourceTable(), xmlBlock,
          packageName);

      final String[] outError = new String[1];

      PackageParser packageParser = new PackageParser();

      Package pkg = null;
      if(RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.O) {
        pkg = ReflectionHelpers.callInstanceMethod(PackageParser.class, packageParser, "parseBaseApk",
            from(String.class, "unused"),
            from(Resources.class, resources),
            from(XmlResourceParser.class, parser),
            from(int.class, 0),
            from(String[].class, outError)
        );
      } else if(RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.L) {
        pkg = ReflectionHelpers.callInstanceMethod(PackageParser.class, packageParser, "parseBaseApk",
            from(Resources.class, resources),
            from(XmlResourceParser.class, parser),
            from(int.class, 0),
            from(String[].class, outError)
        );
      } else {
        pkg = ReflectionHelpers.callInstanceMethod(PackageParser.class, packageParser, "parsePackage",
            from(Resources.class, resources),
            from(XmlResourceParser.class, parser),
            from(int.class, 0),
            from(String[].class, outError)
        );
      }
      if (pkg == null) {
        throw new RuntimeException("dunno (at " + parser.getPositionDescription() + "): " + outError[0]);
        // throw new PackageParserException(mParseError,
        //     apkPath + " (at " + parser.getPositionDescription() + "): " + outError[0]);
      }

      return pkg;
    } catch (Exception e) {
      throw new RuntimeException(e);
      // } catch (Exception e) {
      //   throw new PackageParserException(INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION,
      //       "Failed to read manifest from " + apkPath, e);
    } finally {
      IoUtils.closeQuietly(parser);
    }
  }


}
