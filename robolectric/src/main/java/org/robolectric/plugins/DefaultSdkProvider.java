package org.robolectric.plugins;

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
import org.robolectric.versioning.AndroidVersions.K;
import org.robolectric.versioning.AndroidVersions.L;
import org.robolectric.versioning.AndroidVersions.LMR1;
import org.robolectric.versioning.AndroidVersions.M;
import org.robolectric.versioning.AndroidVersions.N;
import org.robolectric.versioning.AndroidVersions.NMR1;
import org.robolectric.versioning.AndroidVersions.O;
import org.robolectric.versioning.AndroidVersions.OMR1;
import org.robolectric.versioning.AndroidVersions.P;
import org.robolectric.versioning.AndroidVersions.Q;
import org.robolectric.versioning.AndroidVersions.R;
import org.robolectric.versioning.AndroidVersions.S;
import org.robolectric.versioning.AndroidVersions.Sv2;
import org.robolectric.versioning.AndroidVersions.T;
import org.robolectric.versioning.AndroidVersions.U;

/**
 * Robolectric's default {@link SdkProvider}.
 *
 * <p>The list of SDKs is hard-coded. SDKs are obtained from the provided {@link
 * DependencyResolver}.
 */
@SuppressWarnings("NewApi")
@AutoService(SdkProvider.class)
@Priority(Integer.MIN_VALUE)
public class DefaultSdkProvider implements SdkProvider {

  private static final int RUNNING_JAVA_VERSION = Util.getJavaVersion();

  private static final int PREINSTRUMENTED_VERSION = 4;

  private final DependencyResolver dependencyResolver;

  private final SortedMap<Integer, Sdk> knownSdks;

  @Inject
  public DefaultSdkProvider(DependencyResolver dependencyResolver) {
    this.dependencyResolver = Preconditions.checkNotNull(dependencyResolver);
    TreeMap<Integer, Sdk> tmpKnownSdks = new TreeMap<>();
    populateSdks(tmpKnownSdks);

    this.knownSdks = Collections.unmodifiableSortedMap(tmpKnownSdks);
  }

  protected void populateSdks(TreeMap<Integer, Sdk> knownSdks) {
    knownSdks.put(K.SDK_INT, new DefaultSdk(K.SDK_INT, "4.4_r1", "r2", "REL", 8));
    knownSdks.put(L.SDK_INT, new DefaultSdk(L.SDK_INT, "5.0.2_r3", "r0", "REL", 8));
    knownSdks.put(LMR1.SDK_INT, new DefaultSdk(LMR1.SDK_INT, "5.1.1_r9", "r2", "REL", 8));
    knownSdks.put(M.SDK_INT, new DefaultSdk(M.SDK_INT, "6.0.1_r3", "r1", "REL", 8));
    knownSdks.put(N.SDK_INT, new DefaultSdk(N.SDK_INT, "7.0.0_r1", "r1", "REL", 8));
    knownSdks.put(NMR1.SDK_INT, new DefaultSdk(NMR1.SDK_INT, "7.1.0_r7", "r1", "REL", 8));
    knownSdks.put(O.SDK_INT, new DefaultSdk(O.SDK_INT, "8.0.0_r4", "r1", "REL", 8));
    knownSdks.put(OMR1.SDK_INT, new DefaultSdk(OMR1.SDK_INT, "8.1.0", "4611349", "REL", 8));
    knownSdks.put(P.SDK_INT, new DefaultSdk(P.SDK_INT, "9", "4913185-2", "REL", 8));
    knownSdks.put(Q.SDK_INT, new DefaultSdk(Q.SDK_INT, "10", "5803371", "REL", 9));
    knownSdks.put(R.SDK_INT, new DefaultSdk(R.SDK_INT, "11", "6757853", "REL", 9));
    knownSdks.put(S.SDK_INT, new DefaultSdk(S.SDK_INT, "12", "7732740", "REL", 9));
    knownSdks.put(Sv2.SDK_INT, new DefaultSdk(Sv2.SDK_INT, "12.1", "8229987", "REL", 9));
    knownSdks.put(T.SDK_INT, new DefaultSdk(T.SDK_INT, "13", "9030017", "Tiramisu", 9));
    knownSdks.put(U.SDK_INT, new DefaultSdk(U.SDK_INT, "14", "10818077", "REL", 17));
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

      if (Boolean.parseBoolean(System.getProperty("robolectric.usePreinstrumentedJars", "true"))) {
        String version =
            String.join(
                "-",
                getAndroidVersion(),
                "robolectric",
                robolectricVersion,
                "i" + PREINSTRUMENTED_VERSION);
        return new DependencyJar("org.robolectric", "android-all-instrumented", version, null);
      } else {
        String version = String.join("-", getAndroidVersion(), "robolectric", robolectricVersion);
        return new DependencyJar("org.robolectric", "android-all", version, null);
      }
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
