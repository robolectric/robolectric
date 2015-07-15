package org.robolectric;

import android.os.Build;

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
      return method.getName() + getName();
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
      if (config.sdk().length == 0) {
        return true;
      }
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
  public MultiApiRobolectricTestRunner(Class<?> klass) throws Throwable {
    super(klass, Collections.<Runner>emptyList());

    for (Integer integer : getSupportedApis()) {
      runners.add(createTestRunner(integer));
    }
   }

  protected Set<Integer> getSupportedApis() {
    return SdkConfig.getSupportedApis();
  }

  protected TestRunnerForApiVersion createTestRunner(Integer integer) throws InitializationError {
    return new TestRunnerForApiVersion(getTestClass().getJavaClass(), integer);
  }

  @Override
  protected List<Runner> getChildren() {
    return runners;
  }
}
