package org.robolectric.util;

import com.google.common.io.CharStreams;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import org.junit.Test;
import org.junit.runners.model.InitializationError;
import org.robolectric.LegacyDependencyResolver;
import org.robolectric.R;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.internal.Sdk;
import org.robolectric.internal.dependency.DependencyResolver;
import org.robolectric.pluginapi.SdkProvider;
import org.robolectric.plugins.DefaultSdkProvider;
import org.robolectric.res.Fs;
import org.robolectric.res.ResourcePath;

public abstract class TestUtil {
  private static ResourcePath SYSTEM_RESOURCE_PATH;
  private static ResourcePath TEST_RESOURCE_PATH;
  private static File testDirLocation;
  private static LegacyDependencyResolver dependencyResolver;
  private static final SdkProvider sdkProvider = new DefaultSdkProvider();

  public static Path resourcesBaseDir() {
    return resourcesBaseDirFile().toPath();
  }

  private static File resourcesBaseDirFile() {
    if (testDirLocation == null) {
      String baseDir = System.getProperty("robolectric-tests.base-dir");
      return testDirLocation = new File(baseDir, "src/test/resources");
    } else {
      return testDirLocation;
    }
  }

  public static Path resourceFile(String... pathParts) {
    return Fs.join(resourcesBaseDir(), pathParts);
  }

  public static ResourcePath testResources() {
    if (TEST_RESOURCE_PATH == null) {
      TEST_RESOURCE_PATH = new ResourcePath(R.class, resourceFile("res"), resourceFile("assets"));
    }
    return TEST_RESOURCE_PATH;
  }

  public static ResourcePath systemResources() {
    if (SYSTEM_RESOURCE_PATH == null) {
      Sdk sdk = sdkProvider.getMaxSupportedSdk();
      FileSystem fs =
          Fs.forJar(
              getDependencyResolver().getLocalArtifactUrl(sdk.getAndroidSdkDependency()));
      SYSTEM_RESOURCE_PATH =
          new ResourcePath(
              android.R.class, fs.getPath("raw-res/res"), fs.getPath("raw-res/assets"));
    }
    return SYSTEM_RESOURCE_PATH;
  }

  public static ResourcePath sdkResources(int apiLevel) {
    FileSystem sdkResFs =
        Fs.forJar(
            getDependencyResolver()
                .getLocalArtifactUrl(sdkProvider.getSdk(apiLevel).getAndroidSdkDependency()));
    return new ResourcePath(null, sdkResFs.getPath("raw-res/res"), null, null);
  }

  public static String readString(InputStream is) throws IOException {
    return CharStreams.toString(new InputStreamReader(is, "UTF-8"));
  }

  private static DependencyResolver getDependencyResolver() {
    if (dependencyResolver == null) {
      dependencyResolver = new LegacyDependencyResolver(System.getProperties());
    }

    return dependencyResolver;
  }

  public static void resetSystemProperty(String name, String value) {
    if (value == null) {
      System.clearProperty(name);
    } else {
      System.setProperty(name, value);
    }
  }

  private static class MyRobolectricTestRunner extends RobolectricTestRunner {
    MyRobolectricTestRunner() throws InitializationError {
      super(FakeTest.class);
    }

    public static class FakeTest {
      @Test public void fakeTest() {
      }
    }
  }
}
