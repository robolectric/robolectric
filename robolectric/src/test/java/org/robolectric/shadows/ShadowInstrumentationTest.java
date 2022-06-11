package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR2;
import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.N_MR1;
import static com.google.common.truth.Truth.assertThat;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.ActivityThread;
import android.app.Application;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.UserHandle;
import android.view.View;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.lang.reflect.Method;
import java.util.concurrent.CountDownLatch;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

/** Tests for the ShadowInstrumentation class. */
@RunWith(AndroidJUnit4.class)
public class ShadowInstrumentationTest {

  @Test
  @Config(minSdk = JELLY_BEAN_MR1, maxSdk = N_MR1)
  public void testExecStartActivity_handledProperlyForSDK17to25() throws Exception {
    Instrumentation instrumentation =
        ((ActivityThread) RuntimeEnvironment.getActivityThread()).getInstrumentation();

    Intent expectedIntent = new Intent("do_the_thing");

    // Use reflection since the method doesn't exist in the latest SDK.
    Method method =
        Instrumentation.class.getMethod(
            "execStartActivity",
            Context.class,
            IBinder.class,
            IBinder.class,
            Activity.class,
            Intent.class,
            int.class,
            Bundle.class,
            UserHandle.class);
    method.invoke(
        instrumentation,
        instrumentation.getContext(),
        null,
        null,
        null,
        expectedIntent,
        0,
        null,
        null);

    Intent actualIntent =
        shadowOf((Application) ApplicationProvider.getApplicationContext())
            .getNextStartedActivity();

    assertThat(actualIntent).isEqualTo(expectedIntent);
  }

  /**
   * This tests that concurrent startService operations are handled correctly.
   *
   * <p>It runs 2 concurrent threads, each of which call startService 10000 times. This should be
   * enough to trigger any issues related to concurrent state modifications in the
   * ShadowInstrumentation implementation.
   */
  @Test
  @Config(minSdk = N)
  public void concurrentStartService_hasCorrectStartServiceCount() throws InterruptedException {

    HandlerThread background1Thread = new HandlerThread("background1");
    background1Thread.start();
    Handler handler1 = new Handler(background1Thread.getLooper());
    HandlerThread background2Thread = new HandlerThread("background2");
    background2Thread.start();
    Handler handler2 = new Handler(background2Thread.getLooper());

    Intent intent = new Intent("do_the_thing");
    intent.setClassName("com.blah", "com.blah.service");
    Context context = ApplicationProvider.getApplicationContext();
    Runnable startServicesTask =
        () -> {
          for (int i = 0; i < 10000; i++) {
            context.startService(intent);
          }
        };

    CountDownLatch finishedLatch = new CountDownLatch(2);

    handler1.post(startServicesTask);
    handler2.post(startServicesTask);
    handler1.post(finishedLatch::countDown);
    handler2.post(finishedLatch::countDown);

    assertThat(finishedLatch.await(10, SECONDS)).isTrue();

    int intentCount = 0;

    while (true) {
      Intent serviceIntent =
          shadowOf((Application) ApplicationProvider.getApplicationContext())
              .getNextStartedService();
      if (serviceIntent == null) {
        break;
      }
      intentCount++;
    }

    assertThat(intentCount).isEqualTo(20000);
  }

  @Test
  public void setInTouchMode_setFalse() {
    InstrumentationRegistry.getInstrumentation().setInTouchMode(false);

    View decorView =
        Robolectric.buildActivity(Activity.class).setup().get().getWindow().getDecorView();

    assertThat(decorView.isInTouchMode()).isFalse();
  }

  @Test
  public void setInTouchMode_setTrue() {
    InstrumentationRegistry.getInstrumentation().setInTouchMode(true);

    View decorView =
        Robolectric.buildActivity(Activity.class).setup().get().getWindow().getDecorView();

    assertThat(decorView.isInTouchMode()).isTrue();
  }

  @Config(minSdk = JELLY_BEAN_MR2)
  @Test
  public void getUiAutomation() {
    assertThat(InstrumentationRegistry.getInstrumentation().getUiAutomation()).isNotNull();
  }
}
