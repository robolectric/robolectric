package org.robolectric;

import static org.robolectric.util.TestUtil.resourceFile;

import java.lang.reflect.Method;
import java.util.Locale;
import org.junit.runners.model.InitializationError;
import org.robolectric.annotation.Config;

public class TestRunners {

  public static class SelfTest extends RobolectricTestRunner {
    public SelfTest(Class<?> testClass) throws InitializationError {
      super(testClass);
      Locale.setDefault(Locale.ENGLISH);
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
