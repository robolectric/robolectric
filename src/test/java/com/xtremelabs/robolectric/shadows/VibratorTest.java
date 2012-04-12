package com.xtremelabs.robolectric.shadows;

import static com.xtremelabs.robolectric.Robolectric.shadowOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.content.Context;
import android.os.Vibrator;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;

@RunWith(WithTestDefaultsRunner.class)
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
        
        assertThat(shadowVibrator.isVibrating(), is(true));
        assertThat(shadowVibrator.getMilliseconds(), equalTo(5000L));
    }
    
    @Test
    public void vibratePattern() {
        long[] pattern = new long[] { 0, 200 };
        vibrator.vibrate(pattern, 2);
        
        assertThat(shadowVibrator.isVibrating(), is(true));
        assertThat(shadowVibrator.getPattern(), equalTo(pattern));
        assertThat(shadowVibrator.getRepeat(), equalTo(2));
    }
    
    @Test
    public void cancelled() {
        vibrator.vibrate(5000);
        assertThat(shadowVibrator.isVibrating(), is(true));
        assertThat(shadowVibrator.isCancelled(), is(false));
        vibrator.cancel();
        
        assertThat(shadowVibrator.isVibrating(), is(false));
        assertThat(shadowVibrator.isCancelled(), is(true));
    }
}