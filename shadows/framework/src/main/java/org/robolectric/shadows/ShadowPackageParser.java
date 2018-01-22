package org.robolectric.shadows;

import static android.content.pm.PackageManager.INSTALL_PARSE_FAILED_NOT_APK;
import static android.content.pm.PackageManager.INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION;
import static android.content.pm.PackageParser.PARSE_COLLECT_CERTIFICATES;
import static android.os.Trace.TRACE_TAG_PACKAGE_MANAGER;
import static org.robolectric.shadow.api.Shadow.directlyOn;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import android.content.pm.PackageManager;
import android.content.pm.PackageParser;
import android.content.pm.PackageParser.Package;
import android.content.pm.PackageParser.ParseFlags;
import android.content.pm.PackageParser.SigningDetails;
import android.content.pm.Signature;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.os.Bundle;
import android.os.Trace;
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

  private static final String TAG = "ShadowPackageParser";

  private int mParseError;
  private String mArchiveSourcePath;

  @RealObject PackageParser realObject;
  private static String MANIFEST_FILE;

  /** Parses an AndroidManifest.xml file using the framework PackageParser. */
  public static Package callParsePackage(FsFile manifestFile) {
    MANIFEST_FILE = manifestFile.getPath();
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

  /**
   * We only need to implement this method because the framework expects the AndroidManifest.xml
   * file to exist in the root of the apk. We can't set the field statically since its final and
   * therefore inlined by the compiler. We should be able to remove this method and others below for
   * other framework versions if we move to a model that mirrors what the framework expects, e.g:
   * the AndroidManifest.xml at the root of the resources.ap_
   */
  @Implementation(minSdk = Build.VERSION_CODES.JELLY_BEAN, maxSdk = Build.VERSION_CODES.KITKAT)
  public Package parsePackage(
      File sourceFile, String destCodePath, DisplayMetrics metrics, int flags) {
    mParseError = PackageManager.INSTALL_SUCCEEDED;

    XmlResourceParser parser = null;
    AssetManager assmgr = null;
    Resources res = null;
    boolean assetError = true;
    try {
      assmgr = new AssetManager();
      int cookie = mArchiveSourcePath != null ? assmgr.addAssetPath(mArchiveSourcePath) : 1;
      if (cookie != 0) {
        res = new Resources(assmgr, metrics, null);
        parser = assmgr.openXmlResourceParser(cookie, MANIFEST_FILE);
        assetError = false;
      } else {
        Slog.w(TAG, "Failed adding asset path:" + mArchiveSourcePath);
      }
    } catch (Exception e) {
      Slog.w(TAG, "Unable to read AndroidManifest.xml of " + mArchiveSourcePath, e);
    }
    if (assetError) {
      if (assmgr != null) {
        assmgr.close();
      }
      mParseError = PackageManager.INSTALL_PARSE_FAILED_BAD_MANIFEST;
      throw new RuntimeException("Failed to parse Manifest");
    }
    String[] errorText = new String[1];
    Package pkg = null;
    try {
      pkg =
          directlyOn(
              realObject,
              PackageParser.class,
              "parsePackage",
              ReflectionHelpers.ClassParameter.from(Resources.class, res),
              ReflectionHelpers.ClassParameter.from(XmlResourceParser.class, parser),
              ReflectionHelpers.ClassParameter.from(int.class, flags),
              ReflectionHelpers.ClassParameter.from(String[].class, errorText));
    } catch (Exception e) {
      mParseError = INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION;
      throw new RuntimeException("Failed to parse Manifest", e);
    }

    if (pkg == null) {
      parser.close();
      assmgr.close();
      throw new RuntimeException("Failed to parse Manifest" + errorText[0]);
    }

    parser.close();
    assmgr.close();

    if (RuntimeEnvironment.getApiLevel() <= Build.VERSION_CODES.O_MR1) {
      ReflectionHelpers.setField(pkg, "mSignatures", null);
    } else {
      // BEGIN-INTERNAL
      ReflectionHelpers.setField(pkg, "mSigningDetails", SigningDetails.UNKNOWN);
      // END-INTERNAL
    }

    return pkg;
  }

  @Implementation
  public Bundle parseMetaData(
      Resources res, XmlPullParser parser, AttributeSet attrs, Bundle data, String[] outError)
      throws XmlPullParserException, IOException {

    TypedArray sa =
        res.obtainAttributes(attrs, com.android.internal.R.styleable.AndroidManifestMetaData);

    if (data == null) {
      data = new Bundle();
    }

    String name =
        sa.getNonConfigurationString(
            com.android.internal.R.styleable.AndroidManifestMetaData_name, 0);
    if (name == null) {
      outError[0] = "<meta-data> requires an android:name attribute";
      sa.recycle();
      return null;
    }

    name = name.intern();

    TypedValue v = sa.peekValue(com.android.internal.R.styleable.AndroidManifestMetaData_resource);
    if (v != null && v.resourceId != 0) {
      // Slog.i(TAG, "Meta data ref " + name + ": " + v);
      data.putInt(name, v.resourceId);
    } else {
      v = sa.peekValue(com.android.internal.R.styleable.AndroidManifestMetaData_value);
      // Slog.i(TAG, "Meta data " + name + ": " + v);
      if (v != null) {
        if (v.type == TypedValue.TYPE_STRING) {
          CharSequence cs = v.coerceToString();
          data.putString(name, cs != null ? cs.toString().intern() : null);
        } else if (v.type == TypedValue.TYPE_INT_BOOLEAN) {
          data.putBoolean(name, v.data != 0);
        } else if (v.type >= TypedValue.TYPE_FIRST_INT && v.type <= TypedValue.TYPE_LAST_INT) {
          data.putInt(name, v.data);
        } else if (v.type == TypedValue.TYPE_FLOAT) {
          data.putFloat(name, v.getFloat());
        } else {
          if (true) {
            Slog.w(
                TAG,
                "<meta-data> only supports string, integer, float, color, boolean, and "
                    + "resource reference types: "
                    + parser.getName()
                    + " at "
                    + mArchiveSourcePath
                    + " "
                    + parser.getPositionDescription());
          } else {
            outError[0] =
                "<meta-data> only supports string, integer, float, color, boolean, and resource "
                + "reference types";
            data = null;
          }
        }
      } else {
        outError[0] = "<meta-data> requires an android:value or android:resource attribute";
        data = null;
      }
    }

    sa.recycle();

    XmlUtils.skipCurrentTag(parser);

    return data;
  }

  /*
   * Only required because framework expects AndroidManifest.xml at the root of the resources file.
   * See comment on
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
        pkg =
            directlyOn(
                realObject,
                PackageParser.class,
                "parseBaseApk",
                ReflectionHelpers.ClassParameter.from(String.class, apkFile.getAbsolutePath()),
                ReflectionHelpers.ClassParameter.from(Resources.class, res),
                ReflectionHelpers.ClassParameter.from(XmlResourceParser.class, parser),
                ReflectionHelpers.ClassParameter.from(int.class, flags),
                ReflectionHelpers.ClassParameter.from(String[].class, outError));
      } else {
        pkg =
            directlyOn(
                realObject,
                PackageParser.class,
                "parseBaseApk",
                ReflectionHelpers.ClassParameter.from(Resources.class, res),
                ReflectionHelpers.ClassParameter.from(XmlResourceParser.class, parser),
                ReflectionHelpers.ClassParameter.from(int.class, flags),
                ReflectionHelpers.ClassParameter.from(String[].class, outError));
      }

      if (pkg == null) {
        throw new Exception(
            "Parse error at " + parser.getPositionDescription() + "): " + outError[0]);
      }

      pkg.baseCodePath = apkPath;

      if (RuntimeEnvironment.getApiLevel() <= Build.VERSION_CODES.O_MR1) {
        ReflectionHelpers.setField(pkg, "mSignatures", null);
      } else {
        // BEGIN-INTERNAL
        ReflectionHelpers.setField(pkg, "mSigningDetails", SigningDetails.UNKNOWN);
        // END-INTERNAL
      }

      return pkg;
    } catch (Exception e) {
      throw new RuntimeException(
          "Failed to read manifest from "
              + apkPath
              + "Error code: "
              + INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION,
          e);
    } finally {
      IoUtils.closeQuietly(parser);
    }
  }

  /*
   * Only required because framework expects AndroidManifest.xml at the root of the resources file.
   * See comment on
   * {@link #parsePackage(java.io.File, java.lang.String, android.util.DisplayMetrics, int)}
   */
  @Implementation(minSdk = Build.VERSION_CODES.N)
  public static Object parseApkLite(File apkFile, @ParseFlags int flags) {
    final String apkPath = apkFile.getAbsolutePath();

    AssetManager assets = null;
    XmlResourceParser parser = null;
    try {
      assets = new AssetManager();

      int cookie = assets.addAssetPath(apkPath);
      if (cookie == 0) {
        throw new RuntimeException(
            "Failed to parse " + apkPath + "Error code: " + INSTALL_PARSE_FAILED_NOT_APK);
      }

      final DisplayMetrics metrics = new DisplayMetrics();
      metrics.setToDefaults();

      final Resources res = new Resources(assets, metrics, null);
      parser = assets.openXmlResourceParser(cookie, MANIFEST_FILE);

      Signature[] signatures = null;
      Certificate[][] certificates = null;
      Object signatureDetails = null;
      if ((flags & PARSE_COLLECT_CERTIFICATES) != 0) {
        // TODO: factor signature related items out of Package object
        final Package tempPkg = ReflectionHelpers.newInstance(Package.class);

        try {
          directlyOn(
              PackageParser.class,
              "collectCertificates",
              ReflectionHelpers.ClassParameter.from(Package.class, tempPkg),
              ReflectionHelpers.ClassParameter.from(File.class, apkFile),
              ReflectionHelpers.ClassParameter.from(int.class, 0 /*parseFlags*/));
        } finally {
          Trace.traceEnd(TRACE_TAG_PACKAGE_MANAGER);
        }

        if (RuntimeEnvironment.getApiLevel() <= Build.VERSION_CODES.O_MR1) {
          signatures = ReflectionHelpers.getField(tempPkg, "mSignatures");
          certificates = ReflectionHelpers.getField(tempPkg, "mCertificates");
        } else {
          // BEGIN-INTERNAL
          signatureDetails = tempPkg.mSigningDetails;
          // END-INTERNAL
        }
      }

      final AttributeSet attrs = parser;

      // BEGIN-INTERNAL
      if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.P) {
        return directlyOn(
            PackageParser.class,
            "parseApkLite",
            ReflectionHelpers.ClassParameter.from(String.class, apkPath),
            ReflectionHelpers.ClassParameter.from(XmlPullParser.class, parser),
            ReflectionHelpers.ClassParameter.from(AttributeSet.class, attrs),
            ReflectionHelpers.ClassParameter.from(SigningDetails.class, signatureDetails));
      } else
      // END-INTERNAL
      if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.O_MR1) {
        return directlyOn(
            PackageParser.class,
            "parseApkLite",
            ReflectionHelpers.ClassParameter.from(String.class, apkPath),
            ReflectionHelpers.ClassParameter.from(XmlPullParser.class, parser),
            ReflectionHelpers.ClassParameter.from(AttributeSet.class, attrs),
            ReflectionHelpers.ClassParameter.from(Signature[].class, signatures),
            ReflectionHelpers.ClassParameter.from(Certificate[][].class, certificates));
      } else if (RuntimeEnvironment.getApiLevel() >= Build.VERSION_CODES.O) {
        return directlyOn(
            PackageParser.class,
            "parseApkLite",
            ReflectionHelpers.ClassParameter.from(String.class, apkPath),
            ReflectionHelpers.ClassParameter.from(XmlPullParser.class, parser),
            ReflectionHelpers.ClassParameter.from(AttributeSet.class, attrs),
            ReflectionHelpers.ClassParameter.from(int.class, flags),
            ReflectionHelpers.ClassParameter.from(Signature[].class, signatures),
            ReflectionHelpers.ClassParameter.from(Certificate[][].class, certificates));
      } else {
        return directlyOn(
            PackageParser.class,
            "parseApkLite",
            ReflectionHelpers.ClassParameter.from(String.class, apkPath),
            ReflectionHelpers.ClassParameter.from(Resources.class, res),
            ReflectionHelpers.ClassParameter.from(XmlPullParser.class, parser),
            ReflectionHelpers.ClassParameter.from(AttributeSet.class, attrs),
            ReflectionHelpers.ClassParameter.from(int.class, flags),
            ReflectionHelpers.ClassParameter.from(Signature[].class, signatures),
            ReflectionHelpers.ClassParameter.from(Certificate[][].class, certificates));
      }

    } catch (Exception e) {
      throw new RuntimeException(
          "Failed to parse "
              + apkPath
              + "Error code: "
              + INSTALL_PARSE_FAILED_UNEXPECTED_EXCEPTION);
    } finally {
      IoUtils.closeQuietly(parser);
      IoUtils.closeQuietly(assets);
    }
  }
}
