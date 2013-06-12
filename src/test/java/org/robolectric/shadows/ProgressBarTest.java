package org.robolectric.shadows;

import android.widget.ProgressBar;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.res.Attribute;
import org.robolectric.res.ResName;
import org.robolectric.util.TestUtil;

import static java.util.Arrays.asList;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.*;

@RunWith(TestRunners.WithDefaults.class)
public class ProgressBarTest {

  private int[] testValues = {0, 1, 2, 100};
  private ProgressBar progressBar;

  @Before
  public void setUp() {
    progressBar = new ProgressBar(Robolectric.application, new RoboAttributeSet(asList(
        new Attribute(new ResName(TestUtil.SYSTEM_PACKAGE, "attr", "max"), "100", TestUtil.TEST_PACKAGE),
        new Attribute(new ResName(TestUtil.SYSTEM_PACKAGE, "attr", "indeterminate"), "false", TestUtil.TEST_PACKAGE),
        new Attribute(new ResName(TestUtil.SYSTEM_PACKAGE, "attr", "indeterminateOnly"), "false", TestUtil.TEST_PACKAGE)
    ), Robolectric.application.getResources(), null));
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
