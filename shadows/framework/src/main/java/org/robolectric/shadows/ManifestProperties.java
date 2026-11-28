package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.content.pm.PackageManager.Property;
import android.content.pm.parsing.result.ParseInput;
import android.content.pm.parsing.result.ParseResult;
import android.content.pm.parsing.result.ParseTypeImpl;
import android.content.res.TypedArray;
import android.os.Build.VERSION_CODES;
import android.util.Log;
import com.android.internal.pm.parsing.pkg.PackageImpl;
import com.android.internal.pm.parsing.pkg.ParsedPackage;
import com.android.internal.pm.pkg.component.AconfigFlags;
import com.android.internal.pm.pkg.component.ParsedComponent;
import com.android.internal.pm.pkg.parsing.ParsingPackage;
import com.android.internal.pm.pkg.parsing.ParsingPackageUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/**
 * Reads the {@code <property>} elements of an APK's manifest, which the legacy {@code
 * PackageParser} Robolectric uses skips, by parsing the APK with the framework's {@code
 * ParsingPackageUtils}. Only call on S+.
 */
final class ManifestProperties {

  private static final String TAG = "ManifestProperties";

  /** The parse of each APK, which stays the same for the life of the process. */
  private static final Map<String, List<Property>> cache = new HashMap<>();

  private ManifestProperties() {}

  /** Returns the properties declared in the manifest of the APK at {@code apkPath}. */
  static synchronized List<Property> forApk(String apkPath, String packageName) {
    return cache.computeIfAbsent(apkPath, path -> parse(new File(path)));
  }

  private static List<Property> parse(File apk) {
    try {
      return RuntimeEnvironment.getApiLevel() >= VERSION_CODES.VANILLA_ICE_CREAM
          ? InternalParser.parse(apk)
          : parseWithServerParser(apk);
    } catch (RuntimeException | LinkageError e) {
      // The parser can reach framework classes Robolectric lacks, such as SharedUidMigration.
      Log.w(TAG, "Could not read the properties of " + apk, e);
      return Collections.emptyList();
    }
  }

  /** S to U, where {@code parseDefault} supplies its own callback. */
  private static List<Property> parseWithServerParser(File apk) {
    ParseInput input = ParseTypeImpl.forParsingWithoutPlatformCompat();
    ParseResult<?> result =
        RuntimeEnvironment.getApiLevel() <= VERSION_CODES.S_V2
            ? reflector(ParsingPackageUtilsSReflector.class)
                .parseDefault(input, apk, 0, Collections.emptyList(), false)
            : reflector(ParsingPackageUtilsTReflector.class)
                .parseDefault(input, apk, 0, Collections.emptyList(), false);
    if (result.isError()) {
      logError(apk, result);
      return Collections.emptyList();
    }
    Object pkg = result.getResult();
    List<Property> properties = new ArrayList<>(propertiesOf(pkg));
    for (String getter :
        new String[] {"getActivities", "getReceivers", "getServices", "getProviders"}) {
      for (Object component : ReflectionHelpers.<List<?>>callInstanceMethod(pkg, getter)) {
        properties.addAll(propertiesOf(component));
      }
    }
    return properties;
  }

  private static List<Property> propertiesOf(Object packageOrComponent) {
    Map<String, Property> properties =
        ReflectionHelpers.callInstanceMethod(packageOrComponent, "getProperties");
    return new ArrayList<>(properties.values());
  }

  private static void logError(File apk, ParseResult<?> result) {
    Log.w(TAG, "Could not parse " + apk + ": " + result.getErrorMessage(), result.getException());
  }

  /** V+, where the parser moved to {@code com.android.internal}. Not loaded on earlier SDKs. */
  private static final class InternalParser {
    static List<Property> parse(File apk) {
      if (RuntimeEnvironment.getApiLevel() == VERSION_CODES.VANILLA_ICE_CREAM) {
        // On V, loading ParsingPackageUtils reads the device's aconfig flag files and logs an
        // error for each one that is missing, which is all of them under Robolectric.
        ReflectionHelpers.setStaticField(
            AconfigFlags.class, "sTextProtoFilesOnDevice", Collections.emptyList());
      }
      ParseResult<ParsedPackage> result =
          ParsingPackageUtils.parseDefault(
              ParseTypeImpl.forParsingWithoutPlatformCompat(),
              apk,
              0,
              Collections.emptyList(),
              false,
              new Callback());
      if (result.isError()) {
        logError(apk, result);
        return Collections.emptyList();
      }
      ParsedPackage pkg = result.getResult();
      List<Property> properties = new ArrayList<>(pkg.getProperties().values());
      List<ParsedComponent> components = new ArrayList<>();
      components.addAll(pkg.getActivities());
      components.addAll(pkg.getReceivers());
      components.addAll(pkg.getServices());
      components.addAll(pkg.getProviders());
      for (ParsedComponent component : components) {
        properties.addAll(component.getProperties().values());
      }
      return properties;
    }
  }

  /** What the system's {@code PackageParser2} passes, without features or allowlists. */
  private static final class Callback implements ParsingPackageUtils.Callback {
    @Override
    public boolean hasFeature(String feature) {
      return false;
    }

    @Override
    public ParsingPackage startParsingPackage(
        String packageName,
        String baseApkPath,
        String path,
        TypedArray manifestArray,
        boolean isCoreApp) {
      return PackageImpl.forParsing(packageName, baseApkPath, path, manifestArray, isCoreApp, this);
    }

    @Override
    public Set<String> getHiddenApiWhitelistedApps() {
      return Collections.emptySet();
    }

    @Override
    public Set<String> getInstallConstraintsAllowlist() {
      return Collections.emptySet();
    }
  }

  @ForType(className = "android.content.pm.parsing.ParsingPackageUtils")
  interface ParsingPackageUtilsSReflector {
    @Static
    ParseResult<?> parseDefault(
        ParseInput input,
        File file,
        int parseFlags,
        List<?> splitPermissions,
        boolean collectCertificates);
  }

  @ForType(className = "com.android.server.pm.pkg.parsing.ParsingPackageUtils")
  interface ParsingPackageUtilsTReflector {
    @Static
    ParseResult<?> parseDefault(
        ParseInput input,
        File file,
        int parseFlags,
        List<?> splitPermissions,
        boolean collectCertificates);
  }
}
