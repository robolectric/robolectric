package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.TIRAMISU;
import static com.google.common.truth.Truth.assertThat;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Test ShadowProcess */
@RunWith(AndroidJUnit4.class)
public class ShadowProcessTest {
  // The range of thread priority values is specified by
  // android.os.Process#setThreadPriority(int, int), which is [-20,19].
  private static final int THREAD_PRIORITY_HIGHEST = -20;
  private static final int THREAD_PRIORITY_LOWEST = 19;

  @Test
  public void shouldBeZeroWhenNotSet() {
    assertThat(android.os.Process.myPid()).isEqualTo(0);
  }

  @Test
  public void shouldGetMyPidAsSet() {
    ShadowProcess.setPid(3);
    assertThat(android.os.Process.myPid()).isEqualTo(3);
  }

  @Test
  public void shouldGetMyUidAsSet() {
    ShadowProcess.setUid(123);
    assertThat(android.os.Process.myUid()).isEqualTo(123);
  }

  @Test
  public void shouldGetKilledProcess() {
    ShadowProcess.clearKilledProcesses();
    android.os.Process.killProcess(999);
    assertThat(ShadowProcess.wasKilled(999)).isTrue();
  }

  @Test
  public void shouldClearKilledProcessesOnReset() {
    android.os.Process.killProcess(999);
    ShadowProcess.reset();
    assertThat(ShadowProcess.wasKilled(999)).isFalse();
  }

  @Test
  public void shouldClearKilledProcesses() {
    android.os.Process.killProcess(999);
    ShadowProcess.clearKilledProcesses();
    assertThat(ShadowProcess.wasKilled(999)).isFalse();
  }

  @Test
  public void shouldGetMultipleKilledProcesses() {
    ShadowProcess.clearKilledProcesses();
    android.os.Process.killProcess(999);
    android.os.Process.killProcess(123);
    assertThat(ShadowProcess.wasKilled(999)).isTrue();
    assertThat(ShadowProcess.wasKilled(123)).isTrue();
  }

  @Test
  public void myTid_mainThread_returnsCurrentThreadId() {
    assertThat(android.os.Process.myTid()).isEqualTo(Thread.currentThread().getId());
  }

  @Test
  public void myTid_backgroundThread_returnsCurrentThreadId() throws Exception {
    AtomicBoolean ok = new AtomicBoolean(false);

    Thread thread =
        new Thread(() -> ok.set(android.os.Process.myTid() == Thread.currentThread().getId()));
    thread.start();
    thread.join();

    assertThat(ok.get()).isTrue();
  }

  @Test
  public void myTid_returnsDifferentValuesForDifferentThreads() throws Exception {
    AtomicInteger tid1 = new AtomicInteger(0);
    AtomicInteger tid2 = new AtomicInteger(0);

    Thread thread1 =
        new Thread(
            () -> {
              tid1.set(android.os.Process.myTid());
            });
    Thread thread2 =
        new Thread(
            () -> {
              tid2.set(android.os.Process.myTid());
            });
    thread1.start();
    thread2.start();
    thread1.join();
    thread2.join();

    assertThat(tid1).isNotEqualTo(tid2);
  }

  @Test
  public void getThreadPriority_notSet_returnsZero() {
    assertThat(android.os.Process.getThreadPriority(123)).isEqualTo(0);
  }

  @Test
  public void getThreadPriority_returnsThreadPriority() {
    android.os.Process.setThreadPriority(123, android.os.Process.THREAD_PRIORITY_VIDEO);

    assertThat(android.os.Process.getThreadPriority(123))
        .isEqualTo(android.os.Process.THREAD_PRIORITY_VIDEO);
  }

  @Test
  public void getThreadPriority_currentThread_returnsCurrentThreadPriority() {
    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

    assertThat(android.os.Process.getThreadPriority(/*tid=*/ 0))
        .isEqualTo(android.os.Process.THREAD_PRIORITY_AUDIO);
  }

  @Test
  public void setThreadPriorityOneArgument_setsCurrentThreadPriority() {
    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);

    assertThat(android.os.Process.getThreadPriority(android.os.Process.myTid()))
        .isEqualTo(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
  }

  @Test
  public void setThreadPriorityOneArgument_setsCurrentThreadPriority_highestPriority() {
    android.os.Process.setThreadPriority(THREAD_PRIORITY_HIGHEST);

    assertThat(android.os.Process.getThreadPriority(android.os.Process.myTid()))
        .isEqualTo(THREAD_PRIORITY_HIGHEST);
  }

  @Test
  public void setThreadPriorityOneArgument_setsCurrentThreadPriority_lowestPriority() {
    android.os.Process.setThreadPriority(THREAD_PRIORITY_LOWEST);

    assertThat(android.os.Process.getThreadPriority(android.os.Process.myTid()))
        .isEqualTo(THREAD_PRIORITY_LOWEST);
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void shouldGetProcessNameAsSet() {
    ShadowProcess.setProcessName("com.foo.bar:baz");

    assertThat(android.os.Process.myProcessName()).isEqualTo("com.foo.bar:baz");
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void shouldGetProcessNameAsEmptyAfterReset() {
    ShadowProcess.setProcessName("com.foo.bar:baz");

    ShadowProcess.reset();

    assertThat(android.os.Process.myProcessName()).isEmpty();
  }
}

