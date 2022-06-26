package org.robolectric.junit.rules;

import static org.hamcrest.CoreMatchers.instanceOf;

import android.util.Log;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;

/** Tests for {@link ExpectedLogMessagesRule}. */
@RunWith(AndroidJUnit4.class)
public final class ExpectedLogMessagesRuleTest {

  private final ExpectedLogMessagesRule rule = new ExpectedLogMessagesRule();
  private final ExpectedException expectedException = ExpectedException.none();

  @Rule public RuleChain chain = RuleChain.outerRule(expectedException).around(rule);

  @Test
  public void testExpectErrorLogDoesNotFail() {
    Log.e("Mytag", "What's up");
    rule.expectLogMessage(Log.ERROR, "Mytag", "What's up");
  }

  @Test
  public void testExpectWarnLogDoesNotFail() {
    Log.w("Mytag", "What's up");
    rule.expectLogMessage(Log.WARN, "Mytag", "What's up");
  }

  @Test
  public void testAndroidExpectedLogMessagesFailsWithMessage() {
    expectedException.expect(AssertionError.class);
    Log.e("Mytag", "What's up");
  }

  @Test
  public void testAndroidExpectedLogMessagesDoesNotFailWithExpected() {
    rule.expectErrorsForTag("Mytag");
    Log.e("Mytag", "What's up");
  }

  @Test
  public void testNoExpectedMessageFailsTest() {
    expectedException.expect(AssertionError.class);
    rule.expectLogMessage(Log.ERROR, "Mytag", "What's up");
  }

  @Test
  public void testNoExpectedTagFailsTest() {
    expectedException.expect(AssertionError.class);
    rule.expectErrorsForTag("Mytag");
  }

  @Test
  public void testExpectLogMessageWithThrowable() {
    final Throwable throwable = new Throwable("lorem ipsum");
    Log.e("Mytag", "What's up", throwable);
    rule.expectLogMessageWithThrowable(Log.ERROR, "Mytag", "What's up", throwable);
  }

  @Test
  public void testExpectLogMessageWithThrowableMatcher() {
    final IllegalArgumentException exception = new IllegalArgumentException("lorem ipsum");
    Log.e("Mytag", "What's up", exception);
    rule.expectLogMessageWithThrowableMatcher(
        Log.ERROR, "Mytag", "What's up", instanceOf(IllegalArgumentException.class));
  }

  @Test
  public void testMultipleExpectLogMessagee() {
    final Throwable throwable = new Throwable("lorem ipsum");
    Log.e("Mytag", "What's up", throwable);
    Log.e("Mytag", "Message 2");
    Log.e("Mytag", "Message 3", throwable);
    rule.expectLogMessageWithThrowable(Log.ERROR, "Mytag", "What's up", throwable);
    rule.expectLogMessage(Log.ERROR, "Mytag", "Message 2");
    rule.expectLogMessage(Log.ERROR, "Mytag", "Message 3");
  }

  @Test
  public void testExpectedTagFailureOutput() {
    Log.e("TAG1", "message1");
    rule.expectErrorsForTag("TAG1");
    rule.expectErrorsForTag("TAG3"); // Not logged

    expectedException.expect(
        new TypeSafeMatcher<AssertionError>() {
          @Override
          protected boolean matchesSafely(AssertionError error) {
            return error.getMessage().contains("Expected, and observed:     [TAG1]")
                && error.getMessage().contains("Expected, but not observed: [TAG3]");
          }

          @Override
          public void describeTo(Description description) {
            description.appendText("Matches ExpectedLogMessagesRule");
          }
        });
  }

  @Test
  public void testExpectedLogMessageFailureOutput() {
    Log.e("Mytag", "message1");
    Log.e("Mytag", "message2"); // Not expected
    rule.expectLogMessage(Log.ERROR, "Mytag", "message1");
    rule.expectLogMessage(Log.ERROR, "Mytag", "message3"); // Not logged

    expectedException.expect(
        new TypeSafeMatcher<AssertionError>() {
          @Override
          protected boolean matchesSafely(AssertionError error) {
            return error
                    .getMessage()
                    .matches(
                        "[\\s\\S]*Expected, and observed:\\s+\\[LogItem\\{"
                            + "\\s+timeString='.+'"
                            + "\\s+type=6"
                            + "\\s+tag='Mytag'"
                            + "\\s+msg='message1'"
                            + "\\s+throwable=null"
                            + "\\s+}]"
                            + "[\\s\\S]*")
                && error
                    .getMessage()
                    .matches(
                        "[\\s\\S]*Observed, but not expected:\\s+\\[LogItem\\{"
                            + "\\s+timeString='.+'"
                            + "\\s+type=6"
                            + "\\s+tag='Mytag'"
                            + "\\s+msg='message2'"
                            + "\\s+throwable=null"
                            + "\\s+}][\\s\\S]*")
                && error
                    .getMessage()
                    .matches(
                        "[\\s\\S]*Expected, but not observed: \\[ExpectedLogItem\\{timeString='.+',"
                            + " type=6, tag='Mytag', msg='message3'}]"
                            + "[\\s\\S]*");
          }

          @Override
          public void describeTo(Description description) {
            description.appendText("Matches ExpectedLogMessagesRule");
          }
        });
  }

  @Test
  public void testExpectedLogMessageWithMatcherFailureOutput() {
    Log.e("Mytag", "message1");
    Log.e("Mytag", "message2", new IllegalArgumentException()); // Not expected
    rule.expectLogMessage(Log.ERROR, "Mytag", "message1");
    rule.expectLogMessageWithThrowableMatcher(
        Log.ERROR,
        "Mytag",
        "message2",
        instanceOf(UnsupportedOperationException.class)); // Not logged

    String expectedAndObservedPattern =
        "[\\s\\S]*Expected, and observed:\\s+\\[LogItem\\{"
            + "\\s+timeString='.+'"
            + "\\s+type=6"
            + "\\s+tag='Mytag'"
            + "\\s+msg='message1'"
            + "\\s+throwable=null"
            + "\\s+}]"
            + "[\\s\\S]*";
    String observedAndNotExpectedPattern =
        "[\\s\\S]*Observed, but not expected:\\s+\\[LogItem\\{"
            + "\\s+timeString='.+'"
            + "\\s+type=6"
            + "\\s+tag='Mytag'"
            + "\\s+msg='message2'"
            + "\\s+throwable=java.lang.IllegalArgumentException"
            + "(\\s+at .*\\)\\n)+"
            + "\\s+}][\\s\\S]*";
    String expectedNotObservedPattern =
        "[\\s\\S]*Expected, but not observed:"
            + " \\[ExpectedLogItem\\{timeString='.+',"
            + " type=6, tag='Mytag', msg='message2', throwable="
            + ".*UnsupportedOperationException.*}][\\s\\S]*";
    expectedException.expect(
        new TypeSafeMatcher<AssertionError>() {
          @Override
          protected boolean matchesSafely(AssertionError error) {
            return error.getMessage().matches(expectedAndObservedPattern)
                && error.getMessage().matches(observedAndNotExpectedPattern)
                && error.getMessage().matches(expectedNotObservedPattern);
          }

          @Override
          public void describeTo(Description description) {
            description.appendText(
                "Matches ExpectedLogMessagesRule:\n"
                    + expectedAndObservedPattern
                    + "\n"
                    + observedAndNotExpectedPattern
                    + "\n"
                    + expectedNotObservedPattern);
          }
        });
  }
}
