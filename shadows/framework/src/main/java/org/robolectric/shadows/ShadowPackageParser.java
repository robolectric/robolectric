package org.robolectric.shadows;

import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.PackageParser.Package;
import android.content.pm.Signature;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Trace;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Slog;
import libcore.io.IoUtils;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.res.FsFile;
import org.robolectric.util.ReflectionHelpers;
import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.IOException;
import java.security.cert.Certificate;

import static android.content.pm.PackageManager.INSTALL_PARSE_FAILED_NOT_APK;
import static android.content.pm.PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION;
import static android.content.pm.PackageParser.PARSE_COLLECT_CERTIFICATES;
import static android.os.Trace.TRACE_TAG_PACKAGE_MANAGER;
import static org.robolectric.shadow.api.Shadow.directlyOn;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

@Implements(value = PackageParser.class, isInAndroidSdk = false)
public class ShadowPackageParser {

  private static final String TAG = "ShadowPackageParser";

  private int mParseError;
  private String mArchiveSourcePath;

  @RealObject PackageParser realObject;
  private static String MANIFEST_FILE;

  /**
   * Parses an AndroidManifest.xml file using the framework PackageParser.
   */
  public static Package callParsePackage(FsFile manifestFile) {
    MANIFEST_FILE = manifestFile.getPath();
    PackageParser packageParser = new PackageParser();

    try {
      if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.LOLLIPOP) {
        return packageParser.parsePackage(new File(manifestFile.getPath()), 0);
      } else { // JB -> KK
        return ReflectionHelpers.callInstanceMethod(PackageParser.class, packageParser, "parsePackage",
            from(File.class, new File(manifestFile.getPath())),
            from(String.class, manifestFile.getPath()),
            from(DisplayMetrics.class, new DisplayMetrics()),
            from(int.class, 0));
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * We only need to implement this method because the framework expects the AndroidManifest.xml file to exist in the
   * root of the apk. We can't set the field statically since its final and therefore inlined by the compiler. We should
   * be able to remove this method and others below for other framework versions if we move to a model that mirrors
   * what the framework expects, e.g: the AndroidManifest.xml at the root of the resources.ap_
   */
  @Implementation(minSdk = Build.VERSION_CODES.JELLY_BEAN, maxSdk = Build.VERSION_CODES.KITKAT)
  public Package parsePackage(File sourceFile, String destCodePath,
                              DisplayMetrics metrics, int flags) {
    mParseError = PackageManager.INSTALL_SUCCEEDED;

    XmlResourceParser parser = null;
    AssetManager assmgr = null;
    Resources res = null;
    boolean assetError = true;
    try {
      assmgr = new AssetManager();
      int cookie = assmgr.addAssetPath(mArchiveSourcePath);
      if (cookie != 0) {
        res = new Resources(assmgr, metrics, null);
        parser = assmgr.openXmlResourceParser(cookie, MANIFEST_FILE);
        assetError = false;
      } else {
        Slog.w(TAG, "Failed adding asset path:"+mArchiveSourcePath);
      }
    } catch (Exception e) {
      Slog.w(TAG, "Unable to read AndroidManifest.xml of "
          + mArchiveSourcePath, e);
    }
    if (assetError) {
      if (assmgr != null) assmgr.close();
      mParseError = PackageManager.INSTALL_PARSE_FAILED_BAD_MANIFEST;
      return null;
    }
    String[] errorText = new String[1];
    Package pkg = null;
    try {
      pkg = directlyOn(realObject, PackageParser.class, "parsePackage",
          ReflectionHelpers.ClassParameter.from(Resources.class, res),
          ReflectionHelpers.ClassParameter.from(XmlResourceParser.class, parser),
          ReflectionHelpers.ClassParameter.from(int.class, flags),
          ReflectionHelpers.ClassParameter.from(String[].class, errorText));
    } catch (Exception e) {
      mParseError = INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION;
    }


    if (pkg == null) {
      parser.close();
      assmgr.close();
      return null;
    }

    parser.close();
    assmgr.close();

    pkg.mSignatures = null;

    return pkg;
  }

  /*
   * Only required because framework expects AndroidManifest.xml at the root of the resources file. See comment on
   * {@link #parsePackage(java.io.File, java.lang.String, android.util.DisplayMetrics, int)}
   */
  @Implementation(minSdk = Build.VERSION_CODES.LOLLIPOP)
  public Package parseBaseApk(File apkFile, AssetManager assets, int flags) {
    final String apkPath = apkFile.getAbsolutePath();

    mParseError = PackageManager.INSTALL_SUCCEEDED;
    mArchiveSourcePath = apkFile.getAbsolutePath();

    final int cookie = 0;

    Resources res = null;
    XmlResourceParser parser = null;
    try {
      res = new Resources(assets, new DisplayMetrics(), null);

      parser = assets.openXmlResourceParser(cookie, MANIFEST_FILE);

      final String[] outError = new String[1];
      final Package pkg;
      if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.O) {
        pkg = directlyOn(realObject, PackageParser.class, "parseBaseApk",
            ReflectionHelpers.ClassParameter.from(String.class, apkFile.getAbsolutePath()),
            ReflectionHelpers.ClassParameter.from(Resources.class, res),
            ReflectionHelpers.ClassParameter.from(XmlResourceParser.class, parser),
            ReflectionHelpers.ClassParameter.from(int.class, flags),
            ReflectionHelpers.ClassParameter.from(String[].class, outError));
      } else {
        pkg = directlyOn(realObject, PackageParser.class, "parseBaseApk",
            ReflectionHelpers.ClassParameter.from(Resources.class, res),
            ReflectionHelpers.ClassParameter.from(XmlResourceParser.class, parser),
            ReflectionHelpers.ClassParameter.from(int.class, flags),
            ReflectionHelpers.ClassParameter.from(String[].class, outError));
      }

      if (pkg == null) {
        throw new Exception("Parse error at " + parser.getPositionDescription() + "): " + outError[0]);
      }

      pkg.baseCodePath = apkPath;
      pkg.mSignatures = null;

      return pkg;
    } catch (Exception e) {
      throw new RuntimeException("Failed to read manifest from " + apkPath + "Error code: " + INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION, e);
    } finally {
      IoUtils.closeQuietly(parser);
    }
  }

  /*
   * Only required because framework expects AndroidManifest.xml at the root of the resources file. See comment on
   * {@link #parsePackage(java.io.File, java.lang.String, android.util.DisplayMetrics, int)}
   */
  @Implementation(minSdk = Build.VERSION_CODES.N)
  public static Object parseApkLite(File apkFile, int flags) {
    final String apkPath = apkFile.getAbsolutePath();

    AssetManager assets = null;
    XmlResourceParser parser = null;
    try {
      assets = new AssetManager();

      int cookie = assets.addAssetPath(apkPath);
      if (cookie == 0) {
        throw new RuntimeException("Failed to parse " + apkPath + "Error code: " + INSTALL_PARSE_FAILED_NOT_APK);
      }

      final DisplayMetrics metrics = new DisplayMetrics();
      metrics.setToDefaults();

      final Resources res = new Resources(assets, metrics, null);
      parser = assets.openXmlResourceParser(cookie, MANIFEST_FILE);

      final Signature[] signatures;
      final Certificate[][] certificates;
      if ((flags & PARSE_COLLECT_CERTIFICATES) != 0) {
        // TODO: factor signature related items out of Package object
        final Package tempPkg = ReflectionHelpers.newInstance(Package.class);

        try {
          directlyOn(PackageParser.class, "collectCertificates",
              ReflectionHelpers.ClassParameter.from(Package.class, tempPkg),
              ReflectionHelpers.ClassParameter.from(File.class, apkFile),
              ReflectionHelpers.ClassParameter.from(int.class, 0 /*parseFlags*/));
        } finally {
          Trace.traceEnd(TRACE_TAG_PACKAGE_MANAGER);
        }
        signatures = tempPkg.mSignatures;
        certificates = tempPkg.mCertificates;
      } else {
        signatures = null;
        certificates = null;
      }

      final AttributeSet attrs = parser;

      if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.O_MR1) {
        return directlyOn(PackageParser.class, "parseApkLite",
            ReflectionHelpers.ClassParameter.from(String.class, apkPath),
            ReflectionHelpers.ClassParameter.from(XmlPullParser.class, parser),
            ReflectionHelpers.ClassParameter.from(AttributeSet.class, attrs),
            ReflectionHelpers.ClassParameter.from(Signature[].class, signatures),
            ReflectionHelpers.ClassParameter.from(Certificate[][].class, certificates));
      } else if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.O) {
        return directlyOn(PackageParser.class, "parseApkLite",
            ReflectionHelpers.ClassParameter.from(String.class, apkPath),
            ReflectionHelpers.ClassParameter.from(XmlPullParser.class, parser),
            ReflectionHelpers.ClassParameter.from(AttributeSet.class, attrs),
            ReflectionHelpers.ClassParameter.from(int.class, flags),
            ReflectionHelpers.ClassParameter.from(Signature[].class, signatures),
            ReflectionHelpers.ClassParameter.from(Certificate[][].class, certificates));
      } else {
        return directlyOn(PackageParser.class, "parseApkLite",
            ReflectionHelpers.ClassParameter.from(String.class, apkPath),
            ReflectionHelpers.ClassParameter.from(Resources.class, res),
            ReflectionHelpers.ClassParameter.from(XmlPullParser.class, parser),
            ReflectionHelpers.ClassParameter.from(AttributeSet.class, attrs),
            ReflectionHelpers.ClassParameter.from(int.class, flags),
            ReflectionHelpers.ClassParameter.from(Signature[].class, signatures),
            ReflectionHelpers.ClassParameter.from(Certificate[][].class, certificates));
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to parse " + apkPath, e);
    } finally {
      IoUtils.closeQuietly(parser);
      IoUtils.closeQuietly(assets);
    }
  }
}
