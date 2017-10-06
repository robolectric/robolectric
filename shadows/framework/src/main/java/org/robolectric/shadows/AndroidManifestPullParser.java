package org.robolectric.shadows;

import static android.content.pm.PackageManager.*;
import static android.content.pm.PackageManager.MATCH_DIRECT_BOOT_AWARE;
import static android.content.pm.PackageManager.MATCH_DIRECT_BOOT_UNAWARE;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import android.app.Application;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageUserState;
import android.content.res.AssetManager;
import java.util.HashSet;
import org.robolectric.res.Fs;
import org.robolectric.res.builder.XmlBlock;
import android.content.pm.PackageParser;
import android.content.pm.PackageParser.Package;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import libcore.io.IoUtils;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;

public class AndroidManifestPullParser {
  private static final String ANDROID_MANIFEST_FILENAME = "./src/test/resources/AndroidManifest.xml";


  public void parse() {
    String apkPath = ANDROID_MANIFEST_FILENAME;

    XmlResourceParser parser = null;
    try {
      // Application application = RuntimeEnvironment.application;
      // parser = AssetManager.getSystem().openXmlResourceParser(0, ANDROID_MANIFEST_FILENAME);

      XmlBlock xmlBlock = XmlBlock.create(Fs.fileFromPath(ANDROID_MANIFEST_FILENAME), "org.robolectric");
      parser = ShadowAssetManager
          .getXmlResourceParser(RuntimeEnvironment.getAppResourceTable(), xmlBlock, "org.robolectric");

      final String[] outError = new String[1];

      // PackageParser packageParser = new PackageParser();
      // final Package pkg = parseBaseApk(apkPath, res, parser, flags, outError);

      PackageParser packageParser = new PackageParser();
      Resources resources = RuntimeEnvironment.application.getResources();
      // final Package pkg = packageParser.parseBaseApk("dunno",
      //     resources, parser, 0, new String[1]);

      final Package pkg =
              ReflectionHelpers.callInstanceMethod(PackageParser.class, packageParser, "parseBaseApk",
                      from(Resources.class, resources),
                      from(XmlResourceParser.class, parser),
                      from(int.class, 0),
                      from(String[].class, outError)
              );
      // final Package pkg =
      //         ReflectionHelpers.callInstanceMethod(PackageParser.class, packageParser, "parseBaseApk",
      //                 from(String.class, "dunno"),
      //                 from(Resources.class, resources),
      //                 from(XmlResourceParser.class, parser),
      //                 from(int.class, 0),
      //                 from(String[].class, outError)
      //         );
      if (pkg == null) {
        throw new RuntimeException("dunno (at " + parser.getPositionDescription() + "): " + outError[0]);
        // throw new PackageParserException(mParseError,
        //     apkPath + " (at " + parser.getPositionDescription() + "): " + outError[0]);
      }

      int flags =
          GET_ACTIVITIES |
              GET_RECEIVERS |
              GET_SERVICES |
              GET_PROVIDERS |
              GET_INSTRUMENTATION |
              GET_INTENT_FILTERS |
              GET_SIGNATURES |
              GET_RESOLVED_FILTER |
              GET_META_DATA |
              GET_GIDS |
              MATCH_DISABLED_COMPONENTS |
              GET_SHARED_LIBRARY_FILES |
              GET_URI_PERMISSION_PATTERNS |
              GET_PERMISSIONS |
              MATCH_UNINSTALLED_PACKAGES |
              GET_CONFIGURATIONS |
              MATCH_DISABLED_UNTIL_USED_COMPONENTS |
              MATCH_DIRECT_BOOT_UNAWARE |
              MATCH_DIRECT_BOOT_AWARE
          ;

      PackageInfo packageInfo = PackageParser.generatePackageInfo(pkg, new int[]{0}, flags, 0, 0, new HashSet<String>(), new PackageUserState());

      // pkg.setVolumeUuid(volumeUuid);
      // pkg.setApplicationVolumeUuid(volumeUuid);
      // pkg.setBaseCodePath(apkPath);
      pkg.setSignatures(null);

      System.out.println("pkg = " + pkg);
      System.out.println("packageInfo = " + packageInfo);

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
