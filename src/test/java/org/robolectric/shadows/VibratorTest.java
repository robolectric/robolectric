package org.robolectric.shadows;

import android.app.Activity;
import android.content.Context;
import android.os.Vibrator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.shadowOf;

@RunWith(TestRunners.WithDefaults.class)
public class VibratorTest {
    private Vibrator vibrator;
    private ShadowVibrator shadowVibrator;
    
    @Before
    public void before() {
        vibrator = (Vibrator) new Activity().getSystemService(Context.VIBRATOR_SERVICE);
        shadowVibrator = shadowOf(vibrator);
    }
    
    @Test
    public void vibrateMilliseconds() {
        vibrator.vibrate(5000);

        assertThat(shadowVibrator.isVibrating()).isTrue();
        assertThat(shadowVibrator.getMilliseconds()).isEqualTo(5000L);
    }
    
    @Test
    public void vibratePattern() {
        long[] pattern = new long[] { 0, 200 };
        vibrator.vibrate(pattern, 2);

        assertThat(shadowVibrator.isVibrating()).isTrue();
        assertThat(shadowVibrator.getPattern()).isEqualTo(pattern);
        assertThat(shadowVibrator.getRepeat()).isEqualTo(2);
    }
    
    @Test
    public void cancelled() {
        vibrator.vibrate(5000);
        assertThat(shadowVibrator.isVibrating()).isTrue();
        assertThat(shadowVibrator.isCancelled()).isFalse();
        vibrator.cancel();

        assertThat(shadowVibrator.isVibrating()).isFalse();
        assertThat(shadowVibrator.isCancelled()).isTrue();
    }
}