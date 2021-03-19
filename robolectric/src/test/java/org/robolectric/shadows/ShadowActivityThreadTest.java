package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.app.ActivityThread;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.LazyLoadApplication;
import org.robolectric.annotation.LazyLoadApplication.LazyLoad;

/** Tests for the ShadowActivityThread class. */
@RunWith(AndroidJUnit4.class)
public class ShadowActivityThreadTest {

  @LazyLoadApplication(LazyLoad.ON)
  @Test
  public void currentApplicationIsLazyLoaded() {
    RuntimeEnvironment.application = null;
    assertThat(ShadowActivityThread.currentApplication()).isNotNull();
  }

  @Test
  public void getApplication() {
    ActivityThread activityThread = (ActivityThread) ShadowActivityThread.currentActivityThread();
    assertThat(activityThread.getApplication()).isEqualTo(RuntimeEnvironment.getApplication());
  }

  @Test
  public void getInstrumentation() {
    ActivityThread activityThread = (ActivityThread) ShadowActivityThread.currentActivityThread();
    assertThat(activityThread.getInstrumentation())
        .isEqualTo(InstrumentationRegistry.getInstrumentation());
  }
}
