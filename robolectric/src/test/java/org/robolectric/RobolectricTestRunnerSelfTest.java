package org.robolectric;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.junit.Assert.fail;

import android.app.Application;
import android.content.res.Resources;
import android.os.Build;
import android.os.Looper;
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
    assertWithMessage("application")
        .that((Application) ApplicationProvider.getApplicationContext())
        .isInstanceOf(MyTestApplication.class);
    assertWithMessage("onCreate called")
        .that(((MyTestApplication) ApplicationProvider.getApplicationContext()).onCreateWasCalled)
        .isTrue();
    if (RuntimeEnvironment.useLegacyResources()) {
      assertWithMessage("Application resource loader")
          .that(RuntimeEnvironment.getAppResourceTable())
          .isNotNull();
    }
  }

  @Test
  public void shouldSetUpSystemResources() {
    Resources systemResources = Resources.getSystem();
    Resources appResources = ApplicationProvider.getApplicationContext().getResources();

    assertWithMessage("system resources").that(systemResources).isNotNull();

    assertWithMessage("system resource")
        .that(systemResources.getString(android.R.string.copy))
        .isEqualTo(appResources.getString(android.R.string.copy));

    assertWithMessage("app resource").that(appResources.getString(R.string.howdy)).isNotNull();
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
    assertThat(Looper.getMainLooper().getThread()).isSameInstanceAs(Thread.currentThread());
  }

  @Test(timeout = 1000)
  public void whenTestHarnessUsesDifferentThread_shouldStillReportAsMainThread() {
    assertThat(Looper.getMainLooper().getThread()).isSameInstanceAs(Thread.currentThread());
  }

  @Test
  @Config(sdk = Build.VERSION_CODES.KITKAT)
  public void testVersionConfiguration() {
    assertThat(Build.VERSION.SDK_INT)
        .isEqualTo(Build.VERSION_CODES.KITKAT);
    assertThat(Build.VERSION.RELEASE).isEqualTo("4.4");
  }

  @Test public void hamcrestMatchersDontBlowUpDuringLinking() throws Exception {
    org.hamcrest.MatcherAssert.assertThat(true, CoreMatchers.is(true));
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
      onTerminateCalledFromMain =
          Boolean.valueOf(Looper.getMainLooper().getThread() == Thread.currentThread());
    }
  }
}
