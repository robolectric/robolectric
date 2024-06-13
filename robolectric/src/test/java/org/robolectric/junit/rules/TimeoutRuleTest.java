package org.robolectric.junit.rules;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestTimedOutException;

/** Tests for {@link TimeoutRule}. */
@RunWith(AndroidJUnit4.class)
public final class TimeoutRuleTest {

  private final TimeoutRule rule = TimeoutRule.millis(200);

  @Rule public RuleChain chain = RuleChain.outerRule(rule);

  @Test
  public void testNotTimingOutFinishes() throws InterruptedException {
    Thread.sleep(50);
  }

  @Test
  public void testTimingOutIsInterrupted() {
    try {
      Thread.sleep(1000);
      Assert.fail("Should never reach this statement");
    } catch (InterruptedException e) {
      // ignore expected
    }
  }

  @Test
  public void verifyErrorMessage() {
    final TimeoutRule timeoutRule = TimeoutRule.millis(50);
    final Statement threadSleepStatement =
        new Statement() {
          @Override
          public void evaluate() throws Throwable {
            Thread.sleep(1000);
          }
        };
    final Statement wrappedStatement = timeoutRule.apply(threadSleepStatement, Description.EMPTY);

    final TestTimedOutException exception =
        Assert.assertThrows(TestTimedOutException.class, wrappedStatement::evaluate);
    Assert.assertEquals("test timed out after 50 milliseconds", exception.getMessage());
  }
}
