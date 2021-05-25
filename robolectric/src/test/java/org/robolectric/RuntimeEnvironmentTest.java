package org.robolectric;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static com.google.common.truth.TruthJUnit.assume;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import android.app.Application;
import android.content.res.Configuration;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.util.Scheduler;

@RunWith(AndroidJUnit4.class)
public class RuntimeEnvironmentTest {

  @Test
  public void setMainThread_forCurrentThread() {
    assume().that(ShadowLooper.looperMode()).isEqualTo(LooperMode.Mode.LEGACY);

    RuntimeEnvironment.setMainThread(Thread.currentThread());
    assertThat(RuntimeEnvironment.getMainThread()).isSameInstanceAs(Thread.currentThread());
  }

  @Test
  public void setMainThread_forNewThread() {
    assume().that(ShadowLooper.looperMode()).isEqualTo(LooperMode.Mode.LEGACY);

    Thread t = new Thread();
    RuntimeEnvironment.setMainThread(t);
    assertThat(RuntimeEnvironment.getMainThread()).isSameInstanceAs(t);
  }

  @Test
  public void isMainThread_forNewThread_withoutSwitch() throws InterruptedException {
    assume().that(ShadowLooper.looperMode()).isEqualTo(LooperMode.Mode.LEGACY);

    final AtomicBoolean res = new AtomicBoolean();
    final CountDownLatch finished = new CountDownLatch(1);
    Thread t = new Thread() {
      @Override
      public void run() {
        res.set(RuntimeEnvironment.isMainThread());
        finished.countDown();
      }
    };
    RuntimeEnvironment.setMainThread(Thread.currentThread());
    t.start();
    if (!finished.await(1000, MILLISECONDS)) {
      throw new InterruptedException("Thread " + t + " didn't finish timely");
    }
    assertWithMessage("testThread").that(RuntimeEnvironment.isMainThread()).isTrue();
    assertWithMessage("thread t").that(res.get()).isFalse();
  }

  @Test
  public void isMainThread_forNewThread_withSwitch() throws InterruptedException {
    assume().that(ShadowLooper.looperMode()).isEqualTo(LooperMode.Mode.LEGACY);

    final AtomicBoolean res = new AtomicBoolean();
    final CountDownLatch finished = new CountDownLatch(1);
    Thread t = new Thread() {
      @Override
      public void run() {
        res.set(RuntimeEnvironment.isMainThread());
        finished.countDown();
      }
    };
    RuntimeEnvironment.setMainThread(t);
    t.start();
    if (!finished.await(1000, MILLISECONDS)) {
      throw new InterruptedException("Thread " + t + " didn't finish timely");
    }
    assertWithMessage("testThread").that(RuntimeEnvironment.isMainThread()).isFalse();
    assertWithMessage("thread t").that(res.get()).isTrue();
  }

  @Test
  public void isMainThread_withArg_forNewThread_withSwitch() throws InterruptedException {
    assume().that(ShadowLooper.looperMode()).isEqualTo(LooperMode.Mode.LEGACY);

    Thread t = new Thread();
    RuntimeEnvironment.setMainThread(t);
    assertThat(RuntimeEnvironment.isMainThread(t)).isTrue();
  }

  @Test
  public void getSetMasterScheduler() {
    assume().that(ShadowLooper.looperMode()).isEqualTo(LooperMode.Mode.LEGACY);

    Scheduler s = new Scheduler();
    RuntimeEnvironment.setMasterScheduler(s);
    assertThat(RuntimeEnvironment.getMasterScheduler()).isSameInstanceAs(s);
  }

  @Test
  public void testSetQualifiersAddPropagateToApplicationResources() {
    RuntimeEnvironment.setQualifiers("+land");
    Application app = RuntimeEnvironment.getApplication();
    assertThat(app.getResources().getConfiguration().orientation)
        .isEqualTo(Configuration.ORIENTATION_LANDSCAPE);
  }

  @Test
  public void testSetQualifiersReplacePropagateToApplicationResources() {
    RuntimeEnvironment.setQualifiers("land");
    Application app = RuntimeEnvironment.getApplication();
    assertThat(app.getResources().getConfiguration().orientation)
        .isEqualTo(Configuration.ORIENTATION_LANDSCAPE);
  }

}
