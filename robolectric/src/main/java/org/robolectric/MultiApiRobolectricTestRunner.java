package org.robolectric;

import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.robolectric.annotation.Config;
import org.robolectric.internal.SdkConfig;
import org.robolectric.manifest.AndroidManifest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * A test runner for Robolectric that will run a test against multiple API versions.
 */
public class MultiApiRobolectricTestRunner extends Suite {

  protected static class TestRunnerForApiVersion extends RobolectricTestRunner {

    private final String name;
    private final Integer apiVersion;

    TestRunnerForApiVersion(Class<?> type, Integer apiVersion) throws InitializationError {
      super(type);
      this.apiVersion = apiVersion;
      this.name = apiVersion.toString();
    }

    @Override
    protected String getName() {
      return "[" + apiVersion + "]";
    }

    @Override
    protected String testName(final FrameworkMethod method) {
      // IDE focused test runs rely on preservation of the test name; we'll use the
      //   latest supported SDK for focused test runs
      return method.getName() + (apiVersion == SdkConfig.MAX_SDK_VERSION ? "" : getName());
    }

    @Override
    protected void validateConstructor(List<Throwable> errors) {
      validateOnlyOneConstructor(errors);
    }

    @Override
    public String toString() {
      return "TestClassRunnerForParameters " + name;
    }

    @Override
    protected boolean shouldRunApiVersion(Config config) {
      // If no SDK range or set of SDKs is specified default to running all supported APIs
      if (config.minSdk() == -1 && config.maxSdk() == -1 && config.sdk().length == 0) {
        return true;
      }

      // For SDK ranges
      if (config.minSdk() != -1 || config.maxSdk() != -1) {
        if (config.minSdk() <= apiVersion && config.maxSdk() == -1) {
          return true;
        } else if (config.minSdk() == -1 && config.maxSdk() >= apiVersion) {
          return true;
        } else if (config.minSdk() <= apiVersion && config.maxSdk() >= apiVersion) {
          return true;
        }
      }

      // For SDK groups
      for (int sdk : config.sdk()) {
        if (sdk == apiVersion) {
          return true;
        }
      }
      return false;
    }

    @Override
    protected int pickSdkVersion(Config config, AndroidManifest appManifest) {
      return apiVersion;
    }

    @Override
    protected HelperTestRunner getHelperTestRunner(Class bootstrappedTestClass) {
      try {
        return new HelperTestRunner(bootstrappedTestClass) {
          @Override
          protected void validateConstructor(List<Throwable> errors) {
            TestRunnerForApiVersion.this.validateOnlyOneConstructor(errors);
          }

          @Override
          public String toString() {
            return "HelperTestRunner for " + TestRunnerForApiVersion.this.toString();
          }
        };
      } catch (InitializationError initializationError) {
        throw new RuntimeException(initializationError);
      }
    }
  }

  private final ArrayList<Runner> runners = new ArrayList<>();

  /*
   * Only called reflectively. Do not use programmatically.
   */
  public MultiApiRobolectricTestRunner(Class<?> klass, Set<Integer> supportedApis) throws Throwable {
    super(klass, Collections.<Runner>emptyList());

    for (Integer integer : supportedApis) {
      runners.add(createTestRunner(integer));
    }
   }

   public MultiApiRobolectricTestRunner(Class<?> klass) throws Throwable {
    this(klass, SdkConfig.getSupportedApis());
   }

  protected TestRunnerForApiVersion createTestRunner(Integer integer) throws InitializationError {
    return new TestRunnerForApiVersion(getTestClass().getJavaClass(), integer);
  }

  @Override
  protected List<Runner> getChildren() {
    return runners;
  }
}
