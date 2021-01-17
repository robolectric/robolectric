package org.robolectric.android.util.concurrent;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.android.util.concurrent.BackgroundExecutor.runInBackground;

import android.os.Looper;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.io.IOException;
import java.util.concurrent.Callable;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Unit test for {@link BackgroundExecutor} */
@RunWith(AndroidJUnit4.class)
public class BackgroundExecutorTest {

  @Test
  public void forRunnable_doesNotRunOnMainLooper() {
    runInBackground(
        () -> {
          assertThat(Thread.currentThread())
              .isNotSameInstanceAs(Looper.getMainLooper().getThread());
          assertThat(Looper.myLooper()).isNotSameInstanceAs(Looper.getMainLooper());
        });
  }

  @Test
  public void forRunnable_exceptionsPropogated() {
    try {
      runInBackground(
          (Runnable)
              () -> {
                throw new SecurityException("I failed");
              });
      fail("Expects SecurityException!");
    } catch (SecurityException e) {
      assertThat(e).hasMessageThat().isEqualTo("I failed");
    }
  }

  @Test
  public void forCallable_doesNotRunOnMainLooper() {
    boolean result =
        runInBackground(
            () -> {
              assertThat(Thread.currentThread())
                  .isNotSameInstanceAs(Looper.getMainLooper().getThread());
              assertThat(Looper.myLooper()).isNotSameInstanceAs(Looper.getMainLooper());
              return true;
            });
    assertThat(result).isTrue();
  }

  @Test
  public void forCallable_runtimeExceptionsPropogated() {
    try {
      runInBackground(
          (Callable<?>)
              () -> {
                throw new SecurityException("I failed");
              });
      fail("Expects SecurityException!");
    } catch (SecurityException e) {
      assertThat(e).hasMessageThat().isEqualTo("I failed");
    }
  }

  @Test
  public void forCallable_checkedExceptionsWrapped() {
    try {
      runInBackground(
          (Callable<?>)
              () -> {
                throw new IOException("I failed");
              });
    } catch (RuntimeException e) {
      assertThat(e).hasCauseThat().isInstanceOf(IOException.class);
      assertThat(e).hasCauseThat().hasMessageThat().isEqualTo("I failed");
    }
  }
}
