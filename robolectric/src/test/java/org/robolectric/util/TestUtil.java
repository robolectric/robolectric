package org.robolectric.util;

import com.google.common.io.CharStreams;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.Properties;
import org.robolectric.R;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.plugins.SdkCollection;
import org.robolectric.res.Fs;
import org.robolectric.res.ResourcePath;
import org.robolectric.util.inject.Injector;

public abstract class TestUtil {
  private static ResourcePath SYSTEM_RESOURCE_PATH;
  private static ResourcePath TEST_RESOURCE_PATH;
  private static File testDirLocation;
  private static SdkCollection sdkCollection;
  private static final Injector injector = new Injector.Builder()
      .bind(Properties.class, System.getProperties()).build();

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
      Sdk sdk = getSdkCollection().getMaxSupportedSdk();
      Path path = sdk.getJarPath();
      SYSTEM_RESOURCE_PATH =
          new ResourcePath(
              android.R.class, path.resolve("raw-res/res"), path.resolve("raw-res/assets"));
    }
    return SYSTEM_RESOURCE_PATH;
  }

  public static ResourcePath sdkResources(int apiLevel) {
    Path path = getSdkCollection().getSdk(apiLevel).getJarPath();
    return new ResourcePath(null, path.resolve("raw-res/res"), null, null);
  }

  public static String readString(InputStream is) throws IOException {
    return CharStreams.toString(new InputStreamReader(is, "UTF-8"));
  }

  public static synchronized SdkCollection getSdkCollection() {
    if (sdkCollection == null) {
      sdkCollection = getInjectedInstance(SdkCollection.class);
    }
    return sdkCollection;
  }

  public static void resetSystemProperty(String name, String value) {
    if (value == null) {
      System.clearProperty(name);
    } else {
      System.setProperty(name, value);
    }
  }

  private static <T> T getInjectedInstance(Class<T> clazz) {
    return injector.getInstance(clazz);
  }

}
