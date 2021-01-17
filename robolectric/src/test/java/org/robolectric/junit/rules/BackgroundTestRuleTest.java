package org.robolectric.junit.rules;

import static com.google.common.truth.Truth.assertThat;
import static org.hamcrest.Matchers.is;

import android.os.Looper;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;

/** Tests for {@link BackgroundTestRule}. */
@RunWith(AndroidJUnit4.class)
public final class BackgroundTestRuleTest {

  private final BackgroundTestRule rule = new BackgroundTestRule();
  private final ExpectedException expectedException = ExpectedException.none();

  @Rule public RuleChain chain = RuleChain.outerRule(expectedException).around(rule);

  @Test
  @BackgroundTestRule.BackgroundTest
  public void testRunsInBackground() throws Exception {
    assertThat(Looper.myLooper()).isNotEqualTo(Looper.getMainLooper());
  }

  @Test
  public void testNoAnnotation_runsOnMainThread() throws Exception {
    assertThat(Looper.myLooper()).isEqualTo(Looper.getMainLooper());
  }

  @Test
  @BackgroundTestRule.BackgroundTest
  public void testFailInBackground() throws Exception {
    Exception exception = new Exception("Fail!");
    expectedException.expect(is(exception));
    throw exception;
  }
}
