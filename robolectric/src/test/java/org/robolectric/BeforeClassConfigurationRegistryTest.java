package org.robolectric;

import static com.google.common.truth.Truth.assertThat;

import android.graphics.Matrix;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.config.ConfigurationRegistry;
import org.robolectric.internal.AndroidSandbox;
import org.robolectric.plugins.HierarchicalConfigurationStrategy.ConfigurationImpl;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

/**
 * Tests that the {@link ConfigurationRegistry} is set up before code that runs in the sandbox
 * before the first test, such as {@code @BeforeClass} methods, so that it can load classes whose
 * shadows are picked based on the test configuration.
 */
@RunWith(JUnit4.class)
public final class BeforeClassConfigurationRegistryTest {

  @Test
  public void beforeClass_canLoadClassWithConfigurationDependentShadow() throws Exception {
    Result result =
        new JUnitCore().run(new SingleSdkRobolectricTestRunner(UsesGraphicsInBeforeClass.class));

    assertThat(result.getFailures()).isEmpty();
    assertThat(result.getRunCount()).isEqualTo(2);
  }

  @Test
  public void updateConfigurationRegistry_makesConfigurationAvailableInSandbox() throws Exception {
    SingleSdkRobolectricTestRunner runner =
        new SingleSdkRobolectricTestRunner(UsesGraphicsInBeforeClass.class);
    new JUnitCore().run(runner);
    AndroidSandbox sandbox = runner.getLatestSandbox();
    ConfigurationImpl configuration = new ConfigurationImpl();
    configuration.put(GraphicsMode.Mode.class, GraphicsMode.Mode.NATIVE);

    sandbox.updateConfigurationRegistry(configuration);

    Object graphicsMode =
        ReflectionHelpers.callStaticMethod(
            sandbox.bootstrappedClass(ConfigurationRegistry.class),
            "get",
            ClassParameter.from(Class.class, GraphicsMode.Mode.class));
    assertThat(graphicsMode).isEqualTo(GraphicsMode.Mode.NATIVE);
  }

  /**
   * {@link Matrix}'s shadow is chosen by a {@code GraphicsShadowPicker}, which reads the {@code
   * GraphicsMode} from the {@code ConfigurationRegistry}.
   */
  @Config(
      instrumentedPackages =
          "org.robolectric.beforeclassconfigurationregistrytest" /* creates a new sandbox */)
  public static class UsesGraphicsInBeforeClass {
    private static Matrix matrix;

    @BeforeClass
    public static void beforeClass() {
      matrix = new Matrix();
    }

    @Test
    public void matrixCreatedInBeforeClass_isUsable() {
      assertThat(matrix.isIdentity()).isTrue();
    }

    @Test
    public void matrixCreatedInTest_isUsable() {
      assertThat(new Matrix().isIdentity()).isTrue();
    }
  }
}
