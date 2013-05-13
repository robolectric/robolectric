package org.robolectric;

import android.app.Application;
import android.content.res.Resources;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.DisableStrictI18n;
import org.robolectric.annotation.EnableStrictI18n;

import java.lang.reflect.Method;

import static org.junit.Assert.*;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(RobolectricTestRunnerSelfTest.RunnerForTesting.class)
public class RobolectricTestRunnerSelfTest {

  @Test
  public void shouldInitializeAndBindApplicationButNotCallOnCreate() throws Exception {
    assertNotNull(Robolectric.application);
    assertEquals(MyTestApplication.class, Robolectric.application.getClass());
    assertTrue(((MyTestApplication) Robolectric.application).onCreateWasCalled);
    assertNotNull(shadowOf(Robolectric.application).getResourceLoader());
  }

  @Test public void shouldSetUpSystemResources() throws Exception {
    assertNotNull(Resources.getSystem());
    assertEquals(Robolectric.application.getResources().getString(android.R.string.copy),
        Resources.getSystem().getString(android.R.string.copy));

    assertNotNull(Robolectric.application.getResources().getString(R.string.howdy));
    try {
      Resources.getSystem().getString(R.string.howdy);
      fail("should have thrown");
    } catch (Resources.NotFoundException e) {
    }
  }

  @Test
  public void setStaticValue_shouldIgnoreFinalModifier() {
    SdkEnvironment.setStaticValue(android.os.Build.class, "MODEL", "expected value");

    assertEquals("expected value", android.os.Build.MODEL);
  }

  @Test
  @EnableStrictI18n
  public void internalBeforeTest_setsShadowApplicationStrictI18n() {
    assertTrue(Robolectric.getShadowApplication().isStrictI18n());
  }

  @Test
  @DisableStrictI18n
  public void internalBeforeTest_clearsShadowApplicationStrictI18n() {
    assertFalse(Robolectric.getShadowApplication().isStrictI18n());
  }

  @Test
  @Config(qualifiers = "fr")
  public void internalBeforeTest_testValuesResQualifiers() {
    assertEquals("fr", Robolectric.shadowOf(Robolectric.getShadowApplication().getResources().getAssets()).getQualifiers());
  }

  @Test
  public void internalBeforeTest_resetsValuesResQualifiers() {
    assertEquals("", Robolectric.shadowOf(Robolectric.getShadowApplication().getResources().getConfiguration()).getQualifiers());
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
      @Override public Application createApplication(Method method, AndroidManifest appManifest) {
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
