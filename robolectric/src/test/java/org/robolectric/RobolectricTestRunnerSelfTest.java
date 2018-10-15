package org.robolectric;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import android.app.Application;
import android.content.res.Resources;
import android.os.Build;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.hamcrest.CoreMatchers;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

@RunWith(AndroidJUnit4.class)
@Config(application = RobolectricTestRunnerSelfTest.MyTestApplication.class)
public class RobolectricTestRunnerSelfTest {

  @Test
  public void shouldInitializeAndBindApplicationButNotCallOnCreate() {
    assertThat((Application) ApplicationProvider.getApplicationContext())
        .named("application")
        .isInstanceOf(MyTestApplication.class);
    assertThat(((MyTestApplication) ApplicationProvider.getApplicationContext()).onCreateWasCalled)
        .named("onCreate called")
        .isTrue();
    if (RuntimeEnvironment.useLegacyResources()) {
      assertThat(RuntimeEnvironment.getAppResourceTable())
          .named("Application resource loader")
          .isNotNull();
    }
  }

  @Test
  public void shouldSetUpSystemResources() {
    Resources systemResources = Resources.getSystem();
    Resources appResources = ApplicationProvider.getApplicationContext().getResources();

    assertThat(systemResources).named("system resources").isNotNull();

    assertThat(systemResources.getString(android.R.string.copy)).named("system resource")
        .isEqualTo(appResources.getString(android.R.string.copy));

    assertThat(appResources.getString(R.string.howdy)).named("app resource")
      .isNotNull();
    try {
      systemResources.getString(R.string.howdy);
      fail("Expected Exception not thrown");
    } catch (Resources.NotFoundException e) {
    }
  }

  @Test
  public void setStaticValue_shouldIgnoreFinalModifier() {
    ReflectionHelpers.setStaticField(Build.class, "MODEL", "expected value");

    assertThat(Build.MODEL).isEqualTo("expected value");
  }

  @Test
  @Config(qualifiers = "fr")
  public void internalBeforeTest_testValuesResQualifiers() {
    assertThat(RuntimeEnvironment.getQualifiers()).contains("fr");
  }

  @Test
  public void testMethod_shouldBeInvoked_onMainThread() {
    assertThat(RuntimeEnvironment.isMainThread()).isTrue();
  }

  @Test(timeout = 1000)
  public void whenTestHarnessUsesDifferentThread_shouldStillReportAsMainThread() {
    assertThat(RuntimeEnvironment.isMainThread()).isTrue();
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.KITKAT)
  public void testVersionConfiguration() {
    assertThat(Build.VERSION.SDK_INT)
        .isEqualTo(Build.VERSION_CODES.KITKAT);
    assertThat(Build.VERSION.RELEASE).isEqualTo("4.4");
  }

  @Test public void hamcrestMatchersDontBlowUpDuringLinking() throws Exception {
    org.junit.Assert.assertThat(true, CoreMatchers.is(true));
  }

  @AfterClass
  public static void resetStaticState_shouldBeCalled_onMainThread() {
    assertThat(onTerminateCalledFromMain).isTrue();
  }

  private static Boolean onTerminateCalledFromMain = null;

  public static class MyTestApplication extends Application {
    private boolean onCreateWasCalled;

    @Override
    public void onCreate() {
      this.onCreateWasCalled = true;
    }
    
    @Override
    public void onTerminate() {
      onTerminateCalledFromMain = Boolean.valueOf(RuntimeEnvironment.isMainThread());
    }
  }
}
