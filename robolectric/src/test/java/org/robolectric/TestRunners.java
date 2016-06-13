package org.robolectric;

import static org.robolectric.util.TestUtil.resourceFile;

import org.junit.runners.model.InitializationError;
import org.robolectric.annotation.Config;
import org.robolectric.internal.ParallelUniverseInterface;
import org.robolectric.internal.bytecode.ShadowMap;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.ResourceLoader;
import org.robolectric.shadows.ShadowSystemProperties;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Properties;

public class TestRunners {

  public static class WithoutDefaults extends RobolectricTestRunner {
    public WithoutDefaults(Class<?> testClass) throws InitializationError {
      super(testClass);
    }

    @Override protected ShadowMap createShadowMap() {
      // Don't do any class binding except the bare minimum, because that's what we're trying to test here.
      return new ShadowMap.Builder()
          .addShadowClass(ShadowSystemProperties.class)
          .build();
    }

    @Override
    protected AndroidManifest getAppManifest(Config config) {
      return null;
    }

    @Override
    protected void setUpApplicationState(Method method, ParallelUniverseInterface parallelUniverseInterface, ResourceLoader systemResourceLoader, AndroidManifest appManifest, Config config) {
      // Don't do any resource loading or app init, because that's what we're trying to test here.
    }
  }

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

  public static class RealApisWithoutDefaults extends RobolectricTestRunner {
    public RealApisWithoutDefaults(Class<?> testClass) throws InitializationError {
      super(testClass);
    }

    @Override protected ShadowMap createShadowMap() {
      // Don't do any class binding, because that's what we're trying to test here.
      return ShadowMap.EMPTY;
    }

    @Override public AndroidManifest getAppManifest(Config config) {
      // Don't do any resource loading or app init, because that's what we're trying to test here.
      return null;
    }

    @Override
    protected void setUpApplicationState(Method method,
                                         ParallelUniverseInterface parallelUniverseInterface,
                                         ResourceLoader systemResourceLoader, AndroidManifest appManifest, Config config) {
      // Don't do any resource loading or app init, because that's what we're trying to test here.
    }
  }
}
