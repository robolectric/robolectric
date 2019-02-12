package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.N;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.os.UserHandle;
import android.os.UserManager;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Test ShadowProcess */
@RunWith(AndroidJUnit4.class)
public class ShadowProcessTest {

  private UserManager userManager;
  private Context context;

  @Before
  public void setUp() {
    context = ApplicationProvider.getApplicationContext();
    userManager = (UserManager) context.getSystemService(Context.USER_SERVICE);
  }

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
  @Config(minSdk = JELLY_BEAN_MR1)
  public void shouldGetMyUserHandleAsSet() {
    UserHandle someUser = shadowOf(userManager).addUser(10, "secondary_user", 0);
    ShadowProcess.setUserHandle(someUser);
    assertThat(android.os.Process.myUserHandle()).isEqualTo(someUser);
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1, maxSdk = M)
  public void shouldGetOwnerUserHandleWhenUnset() {
    assertThat(android.os.Process.myUserHandle()).isEqualTo(UserHandle.OWNER);
  }

  @Test
  @Config(minSdk = N)
  public void shouldGetSystemUserHandleWhenUnset() {
    assertThat(android.os.Process.myUserHandle()).isEqualTo(UserHandle.SYSTEM);
  }

  @Test
  public void myTid_mainThread_returnsCurrentThreadId() {
    assertThat(android.os.Process.myTid()).isEqualTo(Thread.currentThread().getId());
  }

  @Test
  public void myTid_backgroundThread_returnsCurrentThreadId() throws Exception {
    AtomicBoolean ok = new AtomicBoolean(false);

    Thread thread =
        new Thread(
            () -> {
              ok.set(android.os.Process.myTid() == Thread.currentThread().getId());
            });
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
}

