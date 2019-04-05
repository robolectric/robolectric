package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.TruthJUnit.assume;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import android.view.Choreographer;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.util.TimeUtils;

@RunWith(AndroidJUnit4.class)
public class ShadowLegacyChoreographerTest {

  @Before
  public void setUp() {
    assume().that(ShadowRealisticLooper.useRealisticLooper()).isFalse();
  }

  @Test
  public void setFrameInterval_shouldUpdateFrameInterval() {
    final long frameInterval = 10 * TimeUtils.NANOS_PER_MS;
    ShadowLegacyChoreographer.setFrameInterval(frameInterval);

    final Choreographer instance = ShadowLegacyChoreographer.getInstance();
    long time1 = instance.getFrameTimeNanos();
    long time2 = instance.getFrameTimeNanos();

    assertThat(time2 - time1).isEqualTo(frameInterval);
  }

  @Test
  public void removeFrameCallback_shouldRemoveCallback() {
    Choreographer instance = ShadowLegacyChoreographer.getInstance();
    Choreographer.FrameCallback callback = mock(Choreographer.FrameCallback.class);
    instance.postFrameCallbackDelayed(callback, 1000);
    instance.removeFrameCallback(callback);
    ShadowApplication.getInstance().getForegroundThreadScheduler().advanceToLastPostedRunnable();
    verify(callback, never()).doFrame(anyLong());
  }

  @Test
  public void reset_shouldResetFrameInterval() {
    ShadowLegacyChoreographer.setFrameInterval(1);
    assertThat(ShadowLegacyChoreographer.getFrameInterval()).isEqualTo(1);

    ShadowLegacyChoreographer.reset();
    assertThat(ShadowLegacyChoreographer.getFrameInterval()).isEqualTo(10 * TimeUtils.NANOS_PER_MS);
  }
}
