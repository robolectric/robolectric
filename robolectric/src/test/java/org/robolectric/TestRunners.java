package org.robolectric;

import org.junit.runners.model.InitializationError;
import org.robolectric.annotation.Config;

import java.util.Locale;

import static org.robolectric.util.TestUtil.resourceFile;

public class TestRunners {

  public static class SelfTest extends RobolectricTestRunner {
    public SelfTest(Class<?> testClass) throws InitializationError {
      super(testClass);
      Locale.setDefault(Locale.ENGLISH);
    }

    @Override
    protected Config buildGlobalConfig() {
      return new Config.Builder(super.buildGlobalConfig())
          .setManifest(resourceFile("TestAndroidManifest.xml").toString())
          .build();
    }
  }

  public static class MultiApiSelfTest extends SelfTest {
    public MultiApiSelfTest(Class<?> testClass) throws Throwable {
      super(testClass);
    }

    @Override
    protected Config buildGlobalConfig() {
      return new Config.Builder(super.buildGlobalConfig())
          .setSdk(Config.ALL_SDKS)
          .build();
    }
  }
}
