package org.robolectric.junit.rules;

import android.util.Log;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/** Tests for {@link ExpectedLogMessagesRule}. */
@RunWith(RobolectricTestRunner.class)
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
}
