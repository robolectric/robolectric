package org.robolectric;

import static com.google.common.truth.Truth.assertThat;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.util.Scheduler;

@RunWith(JUnit4.class)
public class RuntimeEnvironmentTest {

  @Test
  public void setMainThread_forCurrentThread() {
    RuntimeEnvironment.setMainThread(Thread.currentThread());
    assertThat(RuntimeEnvironment.getMainThread()).isSameAs(Thread.currentThread());
  }

  @Test
  public void setMainThread_forNewThread() {
    Thread t = new Thread();
    RuntimeEnvironment.setMainThread(t);
    assertThat(RuntimeEnvironment.getMainThread()).isSameAs(t);
  }

  @Test
  public void isMainThread_forNewThread_withoutSwitch() throws InterruptedException {
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
    assertThat(RuntimeEnvironment.isMainThread()).named("testThread").isTrue();
    assertThat(res.get()).named("thread t").isFalse();
  }

  @Test
  public void isMainThread_forNewThread_withSwitch() throws InterruptedException {
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
    assertThat(RuntimeEnvironment.isMainThread()).named("testThread").isFalse();
    assertThat(res.get()).named("thread t").isTrue();
  }

  @Test
  public void isMainThread_withArg_forNewThread_withSwitch() throws InterruptedException {
    Thread t = new Thread();
    RuntimeEnvironment.setMainThread(t);
    assertThat(RuntimeEnvironment.isMainThread(t)).isTrue();
  }

  @Test
  public void getSetMasterScheduler() {
    Scheduler s = new Scheduler();
    RuntimeEnvironment.setMasterScheduler(s);
    assertThat(RuntimeEnvironment.getMasterScheduler()).isSameAs(s);
  }
}