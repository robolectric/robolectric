package com.xtremelabs.robolectric.shadows;

import android.widget.ProgressBar;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(WithTestDefaultsRunner.class)
public class ProgressBarTest {

    private int[] testValues = {0, 1, 2, 100};
    private ProgressBar progressBar;

    @Before
    public void setUp() {
        progressBar = new ProgressBar(null);
    }

    @Test
    public void shouldInitMaxTo100() {
        assertThat(progressBar.getMax(), equalTo(100));
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

    @Test
    public void testSecondaryProgress() {
        for (int progress : testValues) {
            progressBar.setSecondaryProgress(progress);
            assertThat(progressBar.getSecondaryProgress(), equalTo(progress));
        }
    }

    @Test
    public void testIsDeterminate() throws Exception {
        assertFalse(progressBar.isIndeterminate());
        progressBar.setIndeterminate(true);
        assertTrue(progressBar.isIndeterminate());
    }

    @Test
    public void shouldReturnZeroAsProgressWhenIndeterminate() throws Exception {
        progressBar.setProgress(10);
        progressBar.setSecondaryProgress(20);
        progressBar.setIndeterminate(true);
        assertEquals(0, progressBar.getProgress());
        assertEquals(0, progressBar.getSecondaryProgress());
        progressBar.setIndeterminate(false);

        assertEquals(10, progressBar.getProgress());
        assertEquals(20, progressBar.getSecondaryProgress());
    }

    @Test
    public void shouldNotSetProgressWhenIndeterminate() throws Exception {
        progressBar.setIndeterminate(true);
        progressBar.setProgress(10);
        progressBar.setSecondaryProgress(20);
        progressBar.setIndeterminate(false);

        assertEquals(0, progressBar.getProgress());
        assertEquals(0, progressBar.getSecondaryProgress());
    }

    @Test
    public void testIncrementProgressBy() throws Exception {
        assertEquals(0, progressBar.getProgress());
        progressBar.incrementProgressBy(1);
        assertEquals(1, progressBar.getProgress());
        progressBar.incrementProgressBy(1);
        assertEquals(2, progressBar.getProgress());

        assertEquals(0, progressBar.getSecondaryProgress());
        progressBar.incrementSecondaryProgressBy(1);
        assertEquals(1, progressBar.getSecondaryProgress());
        progressBar.incrementSecondaryProgressBy(1);
        assertEquals(2, progressBar.getSecondaryProgress());
    }

    @Test
    public void shouldRespectMax() throws Exception {
        progressBar.setMax(20);
        progressBar.setProgress(50);
        assertEquals(20, progressBar.getProgress());
    }
}
