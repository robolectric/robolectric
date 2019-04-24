package org.robolectric.android.internal;

import static android.os.Looper.getMainLooper;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

import android.os.Handler;
import androidx.test.ext.junit.runners.AndroidJUnit4;
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
    scheduler = new LooperDelegatingScheduler(shadowOf(getMainLooper()));
  }

  @Test
  public void runOneTask() {
    Runnable runnable = mock(Runnable.class);

    new Handler(getMainLooper()).post(runnable);
    verify(runnable, times(0)).run();

    scheduler.runOneTask();
    verify(runnable, times(1)).run();
  }
}
