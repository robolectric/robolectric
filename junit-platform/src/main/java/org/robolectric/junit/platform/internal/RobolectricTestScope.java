package org.robolectric.junit.platform.internal;

import java.lang.reflect.Method;
import javax.annotation.Nullable;
import org.robolectric.DefaultTestLifecycle;
import org.robolectric.TestLifecycle;
import org.robolectric.internal.AndroidSandbox;
import org.robolectric.internal.TestEnvironment;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration;
import org.robolectric.util.Logger;
import org.robolectric.util.ReflectionHelpers;

/**
 * The Android environment of one test, like {@code RobolectricTestRunner#beforeTest}, {@code
 * #afterTest} and {@code #finallyAfterTest}. Must be used on the sandbox's main thread.
 */
public final class RobolectricTestScope implements AutoCloseable {
  private final AndroidSandbox sandbox;
  private final Configuration configuration;
  private final AndroidManifest appManifest;
  private final Method bootstrappedMethod;
  private final TestLifecycle testLifecycle;
  private final TestEnvironment testEnvironment;
  private boolean isSetUp;

  /**
   * @param bootstrappedMethod the test method, as loaded by the sandbox
   * @param testLifecycleClass the {@link TestLifecycle} to use, or null for the default
   */
  public RobolectricTestScope(
      AndroidSandbox sandbox,
      Configuration configuration,
      AndroidManifest appManifest,
      Method bootstrappedMethod,
      @Nullable Class<? extends TestLifecycle> testLifecycleClass) {
    this.sandbox = sandbox;
    this.configuration = configuration;
    this.appManifest = appManifest;
    this.bootstrappedMethod = bootstrappedMethod;
    this.testEnvironment = sandbox.getTestEnvironment();
    Class<TestLifecycle> lifecycleClass =
        sandbox.bootstrappedClass(
            testLifecycleClass == null ? DefaultTestLifecycle.class : testLifecycleClass);
    this.testLifecycle = ReflectionHelpers.newInstance(lifecycleClass);
  }

  public TestLifecycle getTestLifecycle() {
    return testLifecycle;
  }

  public TestEnvironment getTestEnvironment() {
    return testEnvironment;
  }

  /** Configures the sandbox for this test and sets up the application. */
  public void setUp() {
    RobolectricRuntime.get().configureSandbox(sandbox, configuration);
    Logger.lifecycle(
        bootstrappedMethod.getDeclaringClass().getName()
            + "."
            + bootstrappedMethod.getName()
            + ": sdk="
            + sandbox.getSdk().getApiLevel());
    isSetUp = true;
    testEnvironment.setUpApplicationState(
        RobolectricRuntime.tempDirName(bootstrappedMethod), configuration, appManifest);
    testLifecycle.beforeTest(bootstrappedMethod);
  }

  public void prepareTest(Object testInstance) {
    testLifecycle.prepareTest(testInstance);
  }

  public void checkStateAfterTestFailure(Throwable t) throws Throwable {
    testEnvironment.checkStateAfterTestFailure(t);
  }

  /** Tears down the application and resets the Android state. */
  @Override
  public void close() {
    if (!isSetUp) {
      return;
    }
    isSetUp = false;
    try {
      try {
        testEnvironment.tearDownApplication();
      } finally {
        testLifecycle.afterTest(bootstrappedMethod);
      }
    } finally {
      // An interrupted test thread would break AbstractInterruptibleChannels in later tests.
      if (Thread.interrupted()) {
        Logger.warn("Test thread was interrupted! " + bootstrappedMethod);
      }
      testEnvironment.resetState();
    }
  }
}
