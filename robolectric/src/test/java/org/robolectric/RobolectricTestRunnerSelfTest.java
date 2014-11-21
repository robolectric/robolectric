package org.robolectric;

import android.app.Application;
import android.content.res.Resources;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.util.ReflectionHelpers;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

@RunWith(RobolectricTestRunnerSelfTest.RunnerForTesting.class)
public class RobolectricTestRunnerSelfTest {

  @Test
  public void shouldInitializeAndBindApplicationButNotCallOnCreate() throws Exception {
    assertNotNull(RuntimeEnvironment.application);
    assertEquals(MyTestApplication.class, RuntimeEnvironment.application.getClass());
    assertTrue(((MyTestApplication) RuntimeEnvironment.application).onCreateWasCalled);
    assertNotNull(Robolectric.getResourceLoader());
  }

  @Test public void shouldSetUpSystemResources() throws Exception {
    assertNotNull(Resources.getSystem());
    assertEquals(RuntimeEnvironment.application.getResources().getString(android.R.string.copy),
        Resources.getSystem().getString(android.R.string.copy));

    assertNotNull(RuntimeEnvironment.application.getResources().getString(R.string.howdy));
    try {
      Resources.getSystem().getString(R.string.howdy);
      fail("should have thrown");
    } catch (Resources.NotFoundException e) {
    }
  }

  @Test
  public void setStaticValue_shouldIgnoreFinalModifier() {
    ReflectionHelpers.setStaticFieldReflectively(android.os.Build.class, "MODEL", "expected value");

    assertEquals("expected value", android.os.Build.MODEL);
  }

  @Test
  @Config(qualifiers = "fr")
  public void internalBeforeTest_testValuesResQualifiers() {
    String expectedQualifiers = "fr" + TestRunners.WithDefaults.SDK_TARGETED_BY_MANIFEST;
    assertEquals(expectedQualifiers, Shadows.shadowOf(Robolectric.getShadowApplication().getResources().getAssets()).getQualifiers());
  }

  @Test
  public void internalBeforeTest_resetsValuesResQualifiers() {
    assertEquals("", Shadows.shadowOf(Robolectric.getShadowApplication().getResources().getConfiguration()).getQualifiers());
  }

  @Test
  public void internalBeforeTest_doesNotSetI18nStrictModeFromSystemIfPropertyAbsent() {
    assertFalse(Robolectric.getShadowApplication().isStrictI18n());
  }

  public static class RunnerForTesting extends TestRunners.WithDefaults {
    public static RunnerForTesting instance;

    public RunnerForTesting(Class<?> testClass) throws InitializationError {
      super(testClass);
      instance = this;
    }

    @Override protected Class<? extends TestLifecycle> getTestLifecycleClass() {
      return MyTestLifecycle.class;
    }

    public static class MyTestLifecycle extends DefaultTestLifecycle {
      @Override public Application createApplication(Method method, AndroidManifest appManifest, Config config) {
        return new MyTestApplication();
      }
    }
  }

  public static class MyTestApplication extends Application {
    private boolean onCreateWasCalled;

    @Override public void onCreate() {
      this.onCreateWasCalled = true;
    }
  }
}
