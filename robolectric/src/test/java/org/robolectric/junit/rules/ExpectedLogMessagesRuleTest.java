package org.robolectric.junit.rules;

import android.util.Log;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;

/** Tests for {@link ExpectedLogMessagesRule}. */
@RunWith(AndroidJUnit4.class)
public final class ExpectedLogMessagesRuleTest {

  private ExpectedLogMessagesRule rule = new ExpectedLogMessagesRule();
  private ExpectedException expectedException = ExpectedException.none();

  @Rule public RuleChain chain = RuleChain.outerRule(expectedException).around(rule);

  @Test
  public void testAndroidExpectedLogMessagesFailsWithMessage() throws Exception {
    expectedException.expect(AssertionError.class);
    Log.e("Mytag", "What's up");
  }

  @Test
  public void testAndroidExpectedLogMessagesDoesNotFailWithExpected() throws Exception {
    rule.expectErrorsForTag("Mytag");
    Log.e("Mytag", "What's up");
  }

  @Test
  public void testNoExpectedMessageFailsTest() throws Exception {
    expectedException.expect(AssertionError.class);
    rule.expectLogMessage(Log.ERROR, "Mytag", "What's up");
  }

  @Test
  public void testNoExpectedTagFailsTest() throws Exception {
    expectedException.expect(AssertionError.class);
    rule.expectErrorsForTag("Mytag");
  }

  @Test
  public void testExpectLogMessageWithThrowable() {
    final Throwable throwable = new Throwable("lorem ipsum");
    Log.e("Mytag", "What's up", throwable);
    rule.expectLogMessageWithThrowable(Log.ERROR, "Mytag", "What's up", throwable);
  }
}
