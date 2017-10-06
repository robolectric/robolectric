package org.robolectric.shadows;

import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import android.app.Application;
import android.content.res.AssetManager;
import org.robolectric.res.Fs;
import org.robolectric.res.builder.XmlBlock;
import org.robolectric.shadows.PackageParser.Package;
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

      int flags = 0;
      final String[] outError = new String[1];

      // PackageParser packageParser = new PackageParser();
      // final Package pkg = parseBaseApk(apkPath, res, parser, flags, outError);

      PackageParser packageParser = new PackageParser();
      final Package pkg = packageParser.parseBaseApk("dunno", RuntimeEnvironment.application.getResources(), parser, 0, new String[1]);

      // final Package pkg =
      //         ReflectionHelpers.callInstanceMethod(PackageParser.class, packageParser, "parseBaseApk",
      //                 from(String.class, "dunno"),
      //                 from(Resources.class, application.getResources()),
      //                 from(XmlResourceParser.class, parser),
      //                 from(int.class, flags),
      //                 from(String[].class, outError)
      //         );
      if (pkg == null) {
        throw new RuntimeException("dunno (at " + parser.getPositionDescription() + "): " + outError[0]);
        // throw new PackageParserException(mParseError,
        //     apkPath + " (at " + parser.getPositionDescription() + "): " + outError[0]);
      }

      // pkg.setVolumeUuid(volumeUuid);
      // pkg.setApplicationVolumeUuid(volumeUuid);
      // pkg.setBaseCodePath(apkPath);
      pkg.setSignatures(null);

      System.out.println("pkg = " + pkg);

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
