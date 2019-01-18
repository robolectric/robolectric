package org.robolectric.plugins;

import android.os.Build;
import com.google.auto.service.AutoService;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.annotation.Priority;
import javax.inject.Inject;
import org.robolectric.internal.dependency.DependencyJar;
import org.robolectric.internal.dependency.DependencyResolver;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.pluginapi.SdkProvider;
import org.robolectric.util.Util;

/**
 * Robolectric's default {@link SdkProvider}.
 *
 * The list of SDKs is hard-coded. SDKs are obtained from the provided {@link DependencyResolver}.
 */
@SuppressWarnings("NewApi")
@AutoService(SdkProvider.class)
@Priority(Integer.MIN_VALUE)
public class DefaultSdkProvider implements SdkProvider {

  private static final int RUNNING_JAVA_VERSION = Util.getJavaVersion();

  private final DependencyResolver dependencyResolver;

  private final SortedMap<Integer, Sdk> knownApis;

  {
    addSdk(Build.VERSION_CODES.JELLY_BEAN, "4.1.2_r1", "r1", "REL", 8);
    addSdk(Build.VERSION_CODES.JELLY_BEAN_MR1, "4.2.2_r1.2", "r1", "REL", 8);
    addSdk(Build.VERSION_CODES.JELLY_BEAN_MR2, "4.3_r2", "r1", "REL", 8);
    addSdk(Build.VERSION_CODES.KITKAT, "4.4_r1", "r2", "REL", 8);
    addSdk(Build.VERSION_CODES.LOLLIPOP, "5.0.2_r3", "r0", "REL", 8);
    addSdk(Build.VERSION_CODES.LOLLIPOP_MR1, "5.1.1_r9", "r2", "REL", 8);
    addSdk(Build.VERSION_CODES.M, "6.0.1_r3", "r1", "REL", 8);
    addSdk(Build.VERSION_CODES.N, "7.0.0_r1", "r1", "REL", 8);
    addSdk(Build.VERSION_CODES.N_MR1, "7.1.0_r7", "r1", "REL", 8);
    addSdk(Build.VERSION_CODES.O, "8.0.0_r4", "r1", "REL", 8);
    addSdk(Build.VERSION_CODES.O_MR1, "8.1.0", "4611349", "REL", 8);
    addSdk(Build.VERSION_CODES.P, "9", "4913185-2", "REL", 8);

    knownApis = Collections.unmodifiableSortedMap(Setup.knownApis);
  }

  @Inject
  public DefaultSdkProvider(DependencyResolver dependencyResolver) {
    this.dependencyResolver = dependencyResolver;
  }

  @Override
  public Collection<Sdk> getSdks() {
    return Collections.unmodifiableCollection(knownApis.values());
  }

  private void addSdk(int apiLevel, String androidVersion, String frameworkSdkBuildVersion,
      String codeName, int requiredJavaVersion) {
    DefaultSdk sdk =
        new DefaultSdk(apiLevel, androidVersion, frameworkSdkBuildVersion, codeName,
            requiredJavaVersion);

    Setup.knownApis.put(apiLevel, sdk);
  }

  private static class Setup {

    static final TreeMap<Integer, Sdk> knownApis = new TreeMap<>();
  }

  /** Represents an Android SDK stored at Maven Central. */
  public class DefaultSdk extends Sdk {

    private final String androidVersion;
    private final String robolectricVersion;
    private final String codeName;
    private final int requiredJavaVersion;
    private Path jarPath;

    DefaultSdk(
        int apiLevel, String androidVersion, String robolectricVersion, String codeName,
        int requiredJavaVersion) {
      super(apiLevel);
      this.androidVersion = androidVersion;
      this.robolectricVersion = robolectricVersion;
      this.codeName = codeName;
      this.requiredJavaVersion = requiredJavaVersion;
    }

    @Override
    public String getAndroidVersion() {
      return androidVersion;
    }

    @Override
    public String getAndroidCodeName() {
      return codeName;
    }

    private DependencyJar getAndroidSdkDependency() {
      if (!isSupported()) {
        throw new UnsupportedClassVersionError(getUnsupportedMessage());
      }

      return new DependencyJar("org.robolectric",
          "android-all",
          getAndroidVersion() + "-robolectric-" + robolectricVersion, null);
    }

    @Override
    public synchronized Path getJarPath() {
      if (jarPath == null) {
        URL url = dependencyResolver.getLocalArtifactUrl(getAndroidSdkDependency());
        jarPath = Util.pathFrom(url);
      }
      return jarPath;
    }

    @Override
    public boolean isSupported() {
      return requiredJavaVersion <= RUNNING_JAVA_VERSION;
    }

    @Override
    public String getUnsupportedMessage() {
      return String.format(
          Locale.getDefault(),
          "Android SDK %d requires Java %d (have Java %d)",
          getApiLevel(),
          requiredJavaVersion,
          RUNNING_JAVA_VERSION);
    }
  }
}
