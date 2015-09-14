package org.robolectric;

import android.app.Application;
import android.content.res.Resources;

import org.assertj.core.api.Assertions;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.builder.RobolectricPackageManager;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.util.ReflectionHelpers;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunnerSelfTest.RunnerForTesting.class)
public class RobolectricTestRunnerSelfTest {

  @Test
  public void shouldInitializeAndBindApplicationButNotCallOnCreate() {
    assertThat(RuntimeEnvironment.application).as("application")
      .isNotNull()
      .isInstanceOf(MyTestApplication.class);
    assertThat(((MyTestApplication) RuntimeEnvironment.application).onCreateWasCalled).as("onCreate called").isTrue();
    assertThat(ShadowApplication.getInstance().getResourceLoader()).as("resource loader").isNotNull();
  }

  @Test
  public void shouldSetUpSystemResources() {
    assertThat(Resources.getSystem()).as("system resources").isNotNull();
    assertThat(Resources.getSystem().getString(android.R.string.copy)).as("system resource")
      .isEqualTo(RuntimeEnvironment.application.getResources().getString(android.R.string.copy));

    assertThat(RuntimeEnvironment.application.getResources().getString(R.string.howdy)).as("app resource")
      .isNotNull();
    try {
      Resources.getSystem().getString(R.string.howdy);
      Assertions.failBecauseExceptionWasNotThrown(Resources.NotFoundException.class);
    } catch (Resources.NotFoundException e) {
    }
  }

  @Test
  public void setStaticValue_shouldIgnoreFinalModifier() {
    ReflectionHelpers.setStaticField(android.os.Build.class, "MODEL", "expected value");

    assertThat(android.os.Build.MODEL).isEqualTo("expected value");
  }

  @Test
  @Config(qualifiers = "fr")
  public void internalBeforeTest_testValuesResQualifiers() {
    String expectedQualifiers = "fr" + TestRunners.WithDefaults.SDK_TARGETED_BY_MANIFEST;
    assertThat(Shadows.shadowOf(ShadowApplication.getInstance().getResources().getAssets()).getQualifiers()).isEqualTo(expectedQualifiers);
  }

  @Test
  public void internalBeforeTest_resetsValuesResQualifiers() {
    assertThat(Shadows.shadowOf(ShadowApplication.getInstance().getResources().getConfiguration()).getQualifiers())
      .isEqualTo("");
  }

  @Test
  public void internalBeforeTest_doesNotSetI18nStrictModeFromSystemIfPropertyAbsent() {
    assertThat(ShadowApplication.getInstance().isStrictI18n()).isFalse();
  }

  @Before
  public void clearOrder() {
    onTerminateCalledFromMain = null;
    order.clear();
    RobolectricPackageManager mockManager = mock(RobolectricPackageManager.class);
    doAnswer(new Answer<Void>() {
      public Void answer(InvocationOnMock invocation) {
        order.add("reset");
        return null;
      }
    }).when(mockManager).reset();
    
    RuntimeEnvironment.setRobolectricPackageManager(mockManager);
  }

  @Test
  public void testMethod_shouldBeInvoked_onMainThread() {
    assertThat(RuntimeEnvironment.isMainThread()).isTrue();
  }

  @Test(timeout = 1000)
  public void whenTestHarnessUsesDifferentThread_shouldStillReportAsMainThread() {
    assertThat(RuntimeEnvironment.isMainThread()).isTrue();
  }


  @AfterClass
  public static void resetStaticState_shouldBeCalled_afterAppTearDown() {
    assertThat(order).containsExactly("onTerminate", "reset");
  }

  @AfterClass
  public static void resetStaticState_shouldBeCalled_onMainThread() {
    assertThat(onTerminateCalledFromMain).isTrue();
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

  private static List<String> order = new ArrayList<>();
  private static Boolean onTerminateCalledFromMain = null;

  public static class MyTestApplication extends Application {
    private boolean onCreateWasCalled;
    private Boolean onCreateCalledFromMain;

    @Override
    public void onCreate() {
      this.onCreateWasCalled = true;
      this.onCreateCalledFromMain = Boolean.valueOf(RuntimeEnvironment.isMainThread());
    }
    
    @Override
    public void onTerminate() {
      order.add("onTerminate");
      onTerminateCalledFromMain = Boolean.valueOf(RuntimeEnvironment.isMainThread());
    }
  }
}
