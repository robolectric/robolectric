package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.widget.SeekBar;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class SeekBarTest {

    @Test
    public void testOnSeekBarChangedListener() {
        SeekBar seekBar = new SeekBar(new Activity());
        ShadowSeekBar shadow = Robolectric.shadowOf(seekBar);
        SeekBar.OnSeekBarChangeListener listener = new TestSeekBarChangedListener();

        seekBar.setOnSeekBarChangeListener(listener);
        assertThat(shadow.getOnSeekBarChangeListener(), sameInstance(listener));
    }

    private static class TestSeekBarChangedListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }
}
