package com.xtremelabs.robolectric.shadows;

import android.widget.ProgressBar;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class ProgressBarTest {

    private int[] testValues = {0, 1, 2, 100};
    private ProgressBar progressBar;

    @Before
    public void setUp() {
        progressBar = new ProgressBar(null);
    }

    @Test
    public void testMax() {
        for (int max : testValues) {
            progressBar.setMax(max);
            assertThat(progressBar.getMax(), equalTo(max));
        }
    }

    @Test
    public void testProgress() {
        for (int progress : testValues) {
            progressBar.setProgress(progress);
            assertThat(progressBar.getProgress(), equalTo(progress));
        }
    }
}
