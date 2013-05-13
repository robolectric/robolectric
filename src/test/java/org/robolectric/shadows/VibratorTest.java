package org.robolectric.shadows;

import android.app.Activity;
import android.content.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;
import android.os.RoboVibrator;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class VibratorTest {
  private RoboVibrator vibrator;

  @Before
  public void before() {
    vibrator = (RoboVibrator) new Activity().getSystemService(Context.VIBRATOR_SERVICE);
  }

  @Test
  public void vibrateMilliseconds() {
    vibrator.vibrate(5000);

    assertThat(vibrator.isVibrating()).isTrue();
    assertThat(vibrator.getMilliseconds()).isEqualTo(5000L);
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