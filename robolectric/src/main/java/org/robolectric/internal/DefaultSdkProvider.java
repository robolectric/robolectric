package org.robolectric.internal;

import android.os.Build;

import org.robolectric.RobolectricTestRunner;
import org.robolectric.api.Sdk;
import org.robolectric.api.SdkProvider;
import org.robolectric.internal.dependency.DependencyJar;
import org.robolectric.internal.dependency.DependencyResolver;
import org.robolectric.internal.dependency.LocalDependencyResolver;
import org.robolectric.internal.dependency.PropertiesDependencyResolver;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;
import org.robolectric.util.Logger;
import org.robolectric.util.ReflectionHelpers;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class DefaultSdkProvider implements SdkProvider {
  private static final Sdk[] AVAILABLE_SDKS = {
      new ArtifactSdk(Build.VERSION_CODES.JELLY_BEAN, "4.1.2_r1", "4.1.2_r1-robolectric-r1"),
      new ArtifactSdk(Build.VERSION_CODES.JELLY_BEAN_MR1, "4.2.2_r1.2", "4.2.2_r1.2-robolectric-r1"),
      new ArtifactSdk(Build.VERSION_CODES.JELLY_BEAN_MR2, "4.3_r2", "4.3_r2-robolectric-r1"),
      new ArtifactSdk(Build.VERSION_CODES.KITKAT, "4.4_r1", "4.4_r1-robolectric-r2"),
      new ArtifactSdk(Build.VERSION_CODES.LOLLIPOP, "5.0.2_r3", "5.0.2_r3-robolectric-r0"),
      new ArtifactSdk(Build.VERSION_CODES.LOLLIPOP_MR1, "5.1.1_r9", "5.1.1_r9-robolectric-r2"),
      new ArtifactSdk(Build.VERSION_CODES.M, "6.0.1_r3", "6.0.1_r3-robolectric-r1"),
      new ArtifactSdk(Build.VERSION_CODES.N, "7.0.0_r1", "7.0.0_r1-robolectric-r1"),
      new ArtifactSdk(Build.VERSION_CODES.N_MR1, "7.1.0_r7", "7.1.0_r7-robolectric-r1"),
      new ArtifactSdk(Build.VERSION_CODES.O, "8.0.0_r4", "8.0.0_r4-robolectric-r1"),
      new ArtifactSdk(Build.VERSION_CODES.O_MR1, "8.1.0", "8.1.0-robolectric-4611349"),
      new ArtifactSdk(Build.VERSION_CODES.P, "9", "9-robolectric-4913185-2"),
  };

  private final DependencyResolver delegate;

  public DefaultSdkProvider() {
    DependencyResolver dependencyResolver;
    if (Boolean.getBoolean("robolectric.offline")) {
      dependencyResolver = whenOffline();
    } else {
      dependencyResolver = whenOnline();
    }

    URL buildPathPropertiesUrl = getClass().getClassLoader().getResource("robolectric-deps.properties");
    if (buildPathPropertiesUrl != null) {
      Logger.info("Using Robolectric classes from %s", buildPathPropertiesUrl.getPath());

      FsFile propertiesFile = Fs.fileFromPath(buildPathPropertiesUrl.getFile());
      try {
        dependencyResolver = new PropertiesDependencyResolver(propertiesFile, dependencyResolver);
      } catch (IOException e) {
        throw new RuntimeException("couldn't read " + buildPathPropertiesUrl, e);
      }
    }

    this.delegate = dependencyResolver;
  }

  @Override
  public Sdk[] availableSdks() {
    return AVAILABLE_SDKS;
  }

  @Override
  public URL getPathForSdk(Sdk sdk) {
    return delegate.getLocalArtifactUrl(new DependencyJar("org.robolectric", "android-all", ((ArtifactSdk) sdk).artifactVersion, null));
  }

  private DependencyResolver whenOffline() {
    String propPath = System.getProperty("robolectric-deps.properties");
    if (propPath != null) {
      try {
        return new PropertiesDependencyResolver(
            Fs.newFile(propPath),
            null);
      } catch (IOException e) {
        throw new RuntimeException("couldn't read dependencies", e);
      }
    } else {
      String dependencyDir = System.getProperty("robolectric.dependency.dir", ".");
      return new LocalDependencyResolver(new File(dependencyDir));
    }
  }

  private DependencyResolver whenOnline() {
    File cacheDir = new File(new File(System.getProperty("java.io.tmpdir")), "robolectric");

    Class<?> mavenDependencyResolverClass = ReflectionHelpers.loadClass(RobolectricTestRunner.class.getClassLoader(),
        "org.robolectric.internal.dependency.MavenDependencyResolver");
    return (DependencyResolver) ReflectionHelpers.callConstructor(mavenDependencyResolverClass);
  }

  private static class ArtifactSdk extends Sdk {
    private final String artifactVersion;

    public ArtifactSdk(int apiLevel, String androidVersion, String artifactVersion) {
      super(apiLevel);
      this.artifactVersion = artifactVersion;
    }
  }
}
