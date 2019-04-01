package org.robolectric.android.util.concurrent;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.robolectric.android.util.concurrent.BackgroundExecutor.runInBackground;

import android.os.Looper;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Unit test for {@link BackgroundExecutor}
 */
@RunWith(AndroidJUnit4.class)
public class BackgroundExecutorTest {

  @Test
  public void doesNotRunOnMainLooper() {
    runInBackground(() -> {
      assertThat(Thread.currentThread()).isNotSameAs(Looper.getMainLooper().getThread());
      assertThat(Looper.myLooper()).isNotSameAs(Looper.getMainLooper());
    });
  }

  @Test
  public void exceptionsPropogated() {
    try {
      runInBackground(() -> {
        throw new IllegalStateException("I failed");
      });

      fail("did not propagate exception");
    } catch (IllegalStateException e) {
      // expected
    }
  }
}
