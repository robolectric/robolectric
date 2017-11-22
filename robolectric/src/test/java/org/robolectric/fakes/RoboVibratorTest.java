package org.robolectric.fakes;

import static org.assertj.core.api.Assertions.assertThat;

import android.content.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class RoboVibratorTest {
  private RoboVibrator vibrator;

  @Before
  public void before() {
    vibrator = (RoboVibrator) RuntimeEnvironment.application.getSystemService(Context.VIBRATOR_SERVICE);
  }

  @Test
  public void vibrateMilliseconds() {
    vibrator.vibrate(5000);

    assertThat(vibrator.isVibrating()).isTrue();
    assertThat(vibrator.getMilliseconds()).isEqualTo(5000L);

    Robolectric.getForegroundThreadScheduler().advanceToNextPostedRunnable();
    assertThat(vibrator.isVibrating()).isFalse();
  }

  @Test
  public void vibratePattern() {
    long[] pattern = new long[] { 0, 200 };
    vibrator.vibrate(pattern, 2);

    assertThat(vibrator.isVibrating()).isTrue();
    assertThat(vibrator.getPattern()).isEqualTo(pattern);
    assertThat(vibrator.getRepeat()).isEqualTo(2);
  }

  @Test
  public void cancelled() {
    vibrator.vibrate(5000);
    assertThat(vibrator.isVibrating()).isTrue();
    assertThat(vibrator.isCancelled()).isFalse();
    vibrator.cancel();

    assertThat(vibrator.isVibrating()).isFalse();
    assertThat(vibrator.isCancelled()).isTrue();
  }
}
