package org.robolectric.plugins;

import static android.os.Build.VERSION_CODES.JELLY_BEAN;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.O_MR1;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;

import com.google.auto.service.AutoService;
import com.google.common.base.Preconditions;
import java.net.URL;
import java.nio.file.Files;
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

  static final int RUNNING_JAVA_VERSION = Util.getJavaVersion();

  private final DependencyResolver dependencyResolver;

  private final SortedMap<Integer, Sdk> knownSdks;

  @Inject
  public DefaultSdkProvider(DependencyResolver dependencyResolver) {
    this.dependencyResolver = Preconditions.checkNotNull(dependencyResolver);
    TreeMap<Integer, Sdk> tmpKnownSdks = new TreeMap<>();
    if (!SdksFileSdkProvider.populateBuildInjectedSdks(tmpKnownSdks)) {
      populateSdks(tmpKnownSdks);
    }

    this.knownSdks = Collections.unmodifiableSortedMap(tmpKnownSdks);
  }

  protected void populateSdks(TreeMap<Integer, Sdk> knownSdks) {
    knownSdks.put(JELLY_BEAN, new DefaultSdk(JELLY_BEAN, "4.1.2_r1", "r1", "REL", 8));
    knownSdks.put(JELLY_BEAN_MR1, new DefaultSdk(JELLY_BEAN_MR1, "4.2.2_r1.2", "r1", "REL", 8));
    knownSdks.put(JELLY_BEAN_MR2, new DefaultSdk(JELLY_BEAN_MR2, "4.3_r2", "r1", "REL", 8));
    knownSdks.put(KITKAT, new DefaultSdk(KITKAT, "4.4_r1", "r2", "REL", 8));
    knownSdks.put(LOLLIPOP, new DefaultSdk(LOLLIPOP, "5.0.2_r3", "r0", "REL", 8));
    knownSdks.put(LOLLIPOP_MR1, new DefaultSdk(LOLLIPOP_MR1, "5.1.1_r9", "r2", "REL", 8));
    knownSdks.put(M, new DefaultSdk(M, "6.0.1_r3", "r1", "REL", 8));
    knownSdks.put(N, new DefaultSdk(N, "7.0.0_r1", "r1", "REL", 8));
    knownSdks.put(N_MR1, new DefaultSdk(N_MR1, "7.1.0_r7", "r1", "REL", 8));
    knownSdks.put(O, new DefaultSdk(O, "8.0.0_r4", "r1", "REL", 8));
    knownSdks.put(O_MR1, new DefaultSdk(O_MR1, "8.1.0", "4611349", "REL", 8));
    knownSdks.put(P, new DefaultSdk(P, "9", "4913185-2", "REL", 8));
    knownSdks.put(Q, new DefaultSdk(Q, "10", "5803371", "REL", 9));
  }

  @Override
  public Collection<Sdk> getSdks() {
    return Collections.unmodifiableCollection(knownSdks.values());
  }

  /** Represents an Android SDK stored at Maven Central. */
  public class DefaultSdk extends Sdk {

    private final String androidVersion;
    private final String robolectricVersion;
    private final String codeName;
    private final int requiredJavaVersion;
    private Path jarPath;

    public DefaultSdk(
        int apiLevel,
        String androidVersion,
        String robolectricVersion,
        String codeName,
        int requiredJavaVersion) {
      super(apiLevel);
      this.androidVersion = androidVersion;
      this.robolectricVersion = robolectricVersion;
      this.codeName = codeName;
      this.requiredJavaVersion = requiredJavaVersion;
      Preconditions.checkNotNull(dependencyResolver);
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

        if (!Files.exists(jarPath)) {
          throw new RuntimeException("SDK " + getApiLevel() + " jar not present at " + jarPath);
        }
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

    @Override
    public void verifySupportedSdk(String testClassName) {
      if (isKnown() && !isSupported()) {
        throw new UnsupportedOperationException(
            "Failed to create a Robolectric sandbox: " + getUnsupportedMessage());
      }
    }
  }
}
