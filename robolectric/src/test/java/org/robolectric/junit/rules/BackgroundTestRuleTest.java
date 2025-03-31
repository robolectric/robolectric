package org.robolectric.junit.rules;

import static com.google.common.truth.Truth.assertThat;

import android.os.Looper;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link BackgroundTestRule}. */
@RunWith(AndroidJUnit4.class)
public final class BackgroundTestRuleTest {

  @Rule public final BackgroundTestRule rule = new BackgroundTestRule();

  @Test
  @BackgroundTestRule.BackgroundTest
  public void testRunsInBackground() {
    assertThat(Looper.myLooper()).isNotEqualTo(Looper.getMainLooper());
  }

  @Test
  public void testNoAnnotation_runsOnMainThread() {
    assertThat(Looper.myLooper()).isEqualTo(Looper.getMainLooper());
  }

  @Test(expected = Exception.class)
  @BackgroundTestRule.BackgroundTest
  public void testFailInBackground() throws Exception {
    throw new Exception("Fail!");
  }
}
