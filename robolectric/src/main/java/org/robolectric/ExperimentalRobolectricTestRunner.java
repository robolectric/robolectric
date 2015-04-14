package org.robolectric;

import org.junit.Assert;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.robolectric.annotation.Config;
import org.robolectric.internal.SdkConfig;
import org.robolectric.manifest.AndroidManifest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A Parameterized test runner for Robolectric. Copied from the {@link org.junit.runners.Parameterized} class, then modified the custom
 * test runner to extend the {@link org.robolectric.RobolectricTestRunner}. The {@link org.robolectric.RobolectricTestRunner#getHelperTestRunner(Class)}
 * is overridden in order to create instances of the test class with the appropriate apiVersion. Merged in the ability
 * to name your tests through the {@link Parameters#name()} property.
 */
public final class ExperimentalRobolectricTestRunner extends Suite {

  private static class TestClassRunnerForParameters extends RobolectricTestRunner {

    private final String name;
    private final Integer apiVersion;

    TestClassRunnerForParameters(Class<?> type, Integer apiVersion) throws InitializationError {
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
    protected Statement classBlock(RunNotifier notifier) {
      return childrenInvoker(notifier);
    }

    @Override
    public String toString() {
      return "TestClassRunnerForParameters " + name;
    }

    @Override
    protected SdkConfig pickSdkVersion(AndroidManifest appManifest, Config config) {
      return new SdkConfig(apiVersion);
//      if (config != null && config.emulateSdk() > 0) {
//        return new SdkConfig(config.emulateSdk());
//      } else {
//        if (appManifest != null) {
//          return new SdkConfig(appManifest.getTargetSdkVersion());
//        } else {
//          return new SdkConfig(SdkConfig.FALLBACK_SDK_VERSION);
//        }
//      }
    }

    protected int pickReportedSdkVersion(Config config, AndroidManifest appManifest) {
      return apiVersion;
      // Check if the user has explicitly overridden the reported version
//      if (config != null && config.reportSdk() > 0) {
//        return config.reportSdk();
//      }
//      if (config != null && config.emulateSdk() > 0) {
//        return config.emulateSdk();
//      } else {
//        return appManifest != null ? appManifest.getTargetSdkVersion() : SdkConfig.FALLBACK_SDK_VERSION;
//      }
    }

    @Override
    protected HelperTestRunner getHelperTestRunner(Class bootstrappedTestClass) {
      try {
        return new HelperTestRunner(bootstrappedTestClass) {
          @Override
          protected void validateConstructor(List<Throwable> errors) {
            TestClassRunnerForParameters.this.validateOnlyOneConstructor(errors);
          }

          @Override
          public String toString() {
            return "HelperTestRunner for " + TestClassRunnerForParameters.this.toString();
          }
        };
      } catch (InitializationError initializationError) {
        throw new RuntimeException(initializationError);
      }
    }
  }

  private final ArrayList<Runner> runners = new ArrayList<Runner>();

  /*
   * Only called reflectively. Do not use programmatically.
   */
  public ExperimentalRobolectricTestRunner(Class<?> klass) throws Throwable {
    super(klass, Collections.<Runner>emptyList());

    for (Integer integer : SdkConfig.getSupportedApis()) {
      runners.add(new TestClassRunnerForParameters(getTestClass().getJavaClass(), integer));

    }
   }

  @Override
  protected List<Runner> getChildren() {
    return runners;
  }

}
