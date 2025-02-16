package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.TIRAMISU;
import static com.google.common.truth.Truth.assertThat;

import android.os.Process;
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
    assertThat(Process.myPid()).isEqualTo(0);
  }

  @Test
  public void shouldGetMyPidAsSet() {
    ShadowProcess.setPid(3);
    assertThat(Process.myPid()).isEqualTo(3);
  }

  @Test
  public void shouldGetMyUidAsSet() {
    ShadowProcess.setUid(123);
    assertThat(Process.myUid()).isEqualTo(123);
  }

  @Test
  public void shouldGetKilledProcess() {
    ShadowProcess.clearKilledProcesses();
    Process.killProcess(999);
    assertThat(ShadowProcess.wasKilled(999)).isTrue();
  }

  @Test
  public void shouldClearKilledProcessesOnReset() {
    Process.killProcess(999);
    ShadowProcess.reset();
    assertThat(ShadowProcess.wasKilled(999)).isFalse();
  }

  @Test
  public void shouldClearKilledProcesses() {
    Process.killProcess(999);
    ShadowProcess.clearKilledProcesses();
    assertThat(ShadowProcess.wasKilled(999)).isFalse();
  }

  @Test
  public void shouldGetMultipleKilledProcesses() {
    ShadowProcess.clearKilledProcesses();
    Process.killProcess(999);
    Process.killProcess(123);
    assertThat(ShadowProcess.wasKilled(999)).isTrue();
    assertThat(ShadowProcess.wasKilled(123)).isTrue();
  }

  @Test
  public void myTid_mainThread_returnsCurrentThreadId() {
    assertThat(Process.myTid()).isEqualTo(Thread.currentThread().getId());
  }

  @Test
  public void myTid_backgroundThread_returnsCurrentThreadId() throws Exception {
    AtomicBoolean ok = new AtomicBoolean(false);

    Thread thread = new Thread(() -> ok.set(Process.myTid() == Thread.currentThread().getId()));
    thread.start();
    thread.join();

    assertThat(ok.get()).isTrue();
  }

  @Test
  public void myTid_returnsDifferentValuesForDifferentThreads() throws Exception {
    AtomicInteger tid1 = new AtomicInteger(0);
    AtomicInteger tid2 = new AtomicInteger(0);

    Thread thread1 = new Thread(() -> tid1.set(Process.myTid()));
    Thread thread2 = new Thread(() -> tid2.set(Process.myTid()));
    thread1.start();
    thread2.start();
    thread1.join();
    thread2.join();

    assertThat(tid1).isNotEqualTo(tid2);
  }

  @Test
  public void getThreadPriority_notSet_returnsZero() {
    assertThat(Process.getThreadPriority(123)).isEqualTo(0);
  }

  @Test
  public void getThreadPriority_returnsThreadPriority() {
    Process.setThreadPriority(123, Process.THREAD_PRIORITY_VIDEO);

    assertThat(Process.getThreadPriority(123)).isEqualTo(Process.THREAD_PRIORITY_VIDEO);
  }

  @Test
  public void getThreadPriority_currentThread_returnsCurrentThreadPriority() {
    Process.setThreadPriority(Process.THREAD_PRIORITY_AUDIO);

    assertThat(Process.getThreadPriority(/* tid= */ 0)).isEqualTo(Process.THREAD_PRIORITY_AUDIO);
  }

  @Test
  public void setThreadPriorityOneArgument_setsCurrentThreadPriority() {
    Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);

    assertThat(Process.getThreadPriority(Process.myTid()))
        .isEqualTo(Process.THREAD_PRIORITY_URGENT_AUDIO);
  }

  @Test
  public void setThreadPriorityOneArgument_setsCurrentThreadPriority_highestPriority() {
    Process.setThreadPriority(THREAD_PRIORITY_HIGHEST);

    assertThat(Process.getThreadPriority(Process.myTid())).isEqualTo(THREAD_PRIORITY_HIGHEST);
  }

  @Test
  public void setThreadPriorityOneArgument_setsCurrentThreadPriority_lowestPriority() {
    Process.setThreadPriority(THREAD_PRIORITY_LOWEST);

    assertThat(Process.getThreadPriority(Process.myTid())).isEqualTo(THREAD_PRIORITY_LOWEST);
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void shouldGetProcessNameAsSet() {
    ShadowProcess.setProcessName("com.foo.bar:baz");

    assertThat(Process.myProcessName()).isEqualTo("com.foo.bar:baz");
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void shouldGetProcessNameAsEmptyAfterReset() {
    ShadowProcess.setProcessName("com.foo.bar:baz");

    ShadowProcess.reset();

    assertThat(Process.myProcessName()).isEmpty();
  }
}
