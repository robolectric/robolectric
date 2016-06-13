package org.robolectric;

import static org.robolectric.util.TestUtil.resourceFile;

import org.junit.runners.model.InitializationError;
import org.robolectric.annotation.Config;
import org.robolectric.internal.ParallelUniverseInterface;
import org.robolectric.internal.bytecode.ShadowMap;
import org.robolectric.manifest.AndroidManifest;

import java.util.Locale;
import java.util.Properties;

public class TestRunners {

  public static class WithDefaults extends RobolectricTestRunner {
    public static final String SDK_TARGETED_BY_MANIFEST = "-v23";

    public WithDefaults(Class<?> testClass) throws InitializationError {
      super(testClass);
      Locale.setDefault(Locale.ENGLISH);
    }

    @Override
    protected AndroidManifest getAppManifest(Config config) {
      Properties properties = new Properties();
      properties.put("manifest", resourceFile("TestAndroidManifest.xml").toString());
      return super.getAppManifest(
          new Config.Implementation(config, Config.Implementation.fromProperties(properties)));
    }
  }

  public static class MultiApiWithDefaults extends MultiApiRobolectricTestRunner {

    public MultiApiWithDefaults(Class<?> testClass) throws Throwable {
      super(testClass);
      Locale.setDefault(Locale.ENGLISH);
    }

    protected TestRunnerForApiVersion createTestRunner(Integer integer) throws InitializationError {
      return new DefaultRunnerWithApiVersion(getTestClass().getJavaClass(), integer);
    }

    private static class DefaultRunnerWithApiVersion extends TestRunnerForApiVersion {

      DefaultRunnerWithApiVersion(Class<?> type, Integer apiVersion) throws InitializationError {
        super(type, apiVersion);
      }

      @Override
      protected AndroidManifest getAppManifest(Config config) {
        Properties properties = new Properties();
        properties.put("manifest", "src/test/resources/TestAndroidManifest.xml");
        return super.getAppManifest(
            new Config.Implementation(config, Config.Implementation.fromProperties(properties)));
      }
    }
  }
}
