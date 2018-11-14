package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.robolectric.RuntimeEnvironment.application;

import android.util.AttributeSet;
import android.widget.ProgressBar;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

@RunWith(AndroidJUnit4.class)
public class ShadowProgressBarTest {

  private int[] testValues = {0, 1, 2, 100};
  private ProgressBar progressBar;

  @Before
  public void setUp() {
    AttributeSet attrs = Robolectric.buildAttributeSet()
        .addAttribute(android.R.attr.max, "100")
        .addAttribute(android.R.attr.indeterminate, "false")
        .addAttribute(android.R.attr.indeterminateOnly, "false")
        .build();

    progressBar = new ProgressBar(application, attrs);
  }

  @Test
  public void shouldInitMaxTo100() {
    assertThat(progressBar.getMax()).isEqualTo(100);
  }

  @Test
  public void testMax() {
    for (int max : testValues) {
      progressBar.setMax(max);
      assertThat(progressBar.getMax()).isEqualTo(max);
    }
  }

  @Test
  public void testProgress() {
    for (int progress : testValues) {
      progressBar.setProgress(progress);
      assertThat(progressBar.getProgress()).isEqualTo(progress);
    }
  }

  @Test
  public void testSecondaryProgress() {
    for (int progress : testValues) {
      progressBar.setSecondaryProgress(progress);
      assertThat(progressBar.getSecondaryProgress()).isEqualTo(progress);
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
