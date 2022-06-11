package org.robolectric.android.internal;

import static android.os.Looper.getMainLooper;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.os.Handler;
import android.os.SystemClock;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.TimeUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.LooperMode.Mode;
import org.robolectric.util.Scheduler;

@RunWith(AndroidJUnit4.class)
@LooperMode(Mode.PAUSED)
public class LooperDelegatingSchedulerTest {

  private Scheduler scheduler;

  @Before
  public void setUp() {
    scheduler = new LooperDelegatingScheduler(getMainLooper());
  }

  @Test
  public void runOneTask() {
    assertThat(scheduler.runOneTask()).isFalse();

    Runnable runnable = mock(Runnable.class);
    new Handler(getMainLooper()).post(runnable);
    verify(runnable, times(0)).run();

    assertThat(scheduler.runOneTask()).isTrue();
    verify(runnable, times(1)).run();
  }

  @Test
  public void advanceTo() {
    assertThat(scheduler.advanceTo(0)).isFalse();

    assertThat(scheduler.advanceTo(SystemClock.uptimeMillis())).isFalse();

    Runnable runnable = mock(Runnable.class);
    new Handler(getMainLooper()).post(runnable);
    verify(runnable, times(0)).run();

    assertThat(scheduler.advanceTo(SystemClock.uptimeMillis())).isTrue();
    verify(runnable, times(1)).run();
  }

  @Test
  public void advanceBy() {
    Runnable runnable = mock(Runnable.class);
    new Handler(getMainLooper()).postDelayed(runnable, 100);
    verify(runnable, times(0)).run();

    assertThat(scheduler.advanceBy(100, TimeUnit.MILLISECONDS)).isTrue();
    verify(runnable, times(1)).run();
  }

  @Test
  public void size() {
    assertThat(scheduler.size()).isEqualTo(0);

    Runnable runnable = mock(Runnable.class);
    new Handler(getMainLooper()).post(runnable);
    assertThat(scheduler.size()).isEqualTo(1);
    assertThat(scheduler.advanceBy(0, TimeUnit.MILLISECONDS)).isTrue();
    assertThat(scheduler.size()).isEqualTo(0);
  }
}
