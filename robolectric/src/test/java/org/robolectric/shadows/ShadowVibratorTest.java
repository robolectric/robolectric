package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static org.assertj.core.api.Assertions.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.os.Vibrator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
public class ShadowVibratorTest {
  private Vibrator vibrator;

  @Before
  public void before() {
    vibrator = (Vibrator) RuntimeEnvironment.application.getSystemService(Context.VIBRATOR_SERVICE);
  }

  @Test
  public void hasVibrator() {
    assertThat(vibrator.hasVibrator()).isTrue();

    shadowOf(vibrator).setHasVibrator(false);

    assertThat(vibrator.hasVibrator()).isFalse();
  }

  @Config(minSdk = O)
  @Test
  public void hasAmplitudeControl() {
    assertThat(vibrator.hasAmplitudeControl()).isFalse();

    shadowOf(vibrator).setHasAmplitudeControl(true);

    assertThat(vibrator.hasAmplitudeControl()).isTrue();
  }

  @Test
  public void vibrateMilliseconds() {
    vibrator.vibrate(5000);

    assertThat(shadowOf(vibrator).isVibrating()).isTrue();
    assertThat(shadowOf(vibrator).getMilliseconds()).isEqualTo(5000L);

    Robolectric.getForegroundThreadScheduler().advanceToNextPostedRunnable();
    assertThat(shadowOf(vibrator).isVibrating()).isFalse();
  }

  @Test
  public void vibratePattern() {
    long[] pattern = new long[] { 0, 200 };
    vibrator.vibrate(pattern, 1);

    assertThat(shadowOf(vibrator).isVibrating()).isTrue();
    assertThat(shadowOf(vibrator).getPattern()).isEqualTo(pattern);
    assertThat(shadowOf(vibrator).getRepeat()).isEqualTo(1);
  }

  @Test
  public void cancelled() {
    vibrator.vibrate(5000);
    assertThat(shadowOf(vibrator).isVibrating()).isTrue();
    assertThat(shadowOf(vibrator).isCancelled()).isFalse();
    vibrator.cancel();

    assertThat(shadowOf(vibrator).isVibrating()).isFalse();
    assertThat(shadowOf(vibrator).isCancelled()).isTrue();
  }
}
