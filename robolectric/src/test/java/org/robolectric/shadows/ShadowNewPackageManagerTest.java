package org.robolectric.shadows;

import java.lang.reflect.Method;
import org.junit.runner.RunWith;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.internal.bytecode.Sandbox;
import org.robolectric.shadows.ShadowNewPackageManagerTest.ManifestParserTestRunner;

/** Test shadow package mgr with new framework manifest parser */
@RunWith(ManifestParserTestRunner.class)
public final class ShadowNewPackageManagerTest extends ShadowPackageManagerTest {

  /**
   * Custom test runner that turns on framework manifest parsing.
   *
   * <p>A custom test runnner is needed because it needs to change state before
   * setUpApplicationState is called.
   */
  public static class ManifestParserTestRunner extends RobolectricTestRunner {

    public ManifestParserTestRunner(Class<?> testClass) throws InitializationError {
      super(testClass);
    }

    @Override
    protected void beforeTest(Sandbox sandbox, FrameworkMethod method, Method bootstrappedMethod)
        throws Throwable {
      System.setProperty("use_framework_manifest_parser", "true");
      super.beforeTest(sandbox, method, bootstrappedMethod);
    }

    @Override
    protected void afterTest(FrameworkMethod method, Method bootstrappedMethod) {
      System.setProperty("use_framework_manifest_parser", "false");
      super.afterTest(method, bootstrappedMethod);
    }
  }
}
