package org.robolectric.junit.rules;

import static org.hamcrest.CoreMatchers.equalTo;

import android.util.Log;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Matcher;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.ShadowLog.LogItem;

/**
 * Allows tests to assert about the presence of log messages, and turns logged errors that are not
 * explicitly expected into test failures.
 *
 * <h3>Null Expectations</h3>
 *
 * <p>It is permitted to pass {@code null} for any expected value or matcher (i.e. for expected
 * tags, messages or throwables) and this cause the expectation to ignore that attribute during
 * matching. For example:
 *
 * <pre>{@code
 * // Matches any INFO level log statement with the specified tag.
 * logged.expectLogMessage(Log.INFO, "tag", null);
 * // Matches any INFO level log statement with the message "expected", regardless of the tag.
 * logged.expectLogMessage(Log.INFO, null, "message");
 * }<pre>
 *
 * <p>However in general it is not recommended to use this behaviour, since it can cause tests to
 * pass when they should have failed.
 *
 * <h3>Matchers Vs Expected Values</h3>
 *
 * <p>Using a {code Matcher} which can support substring matching and other non-trivial behaviour
 * can be a good way to avoid brittle tests. However there is a  difference between the behaviour of
 * methods which accept Hamcrest matchers and those which only accept expected value (e.g. {@code
 * String} or {@code Pattern}).
 *
 * If an expected value is used to match a log statement, then duplicate expectations will be
 * removed:
 *
 * <pre>{@code
 * logged.expectLogMessage(Log.INFO, "tag", "exact message");
 * // This call has no effect and only 1 log statements is expected.
 * logged.expectLogMessage(Log.INFO, "tag", "exact message");
 * }<pre>
 *
 * <p>When using a {@code Matcher} in any parameter, the existence of duplicate expectations can no
 * longer be determined, so de-duplication does not occur:
 *
 * <pre>{@code
 * logged.expectLogMessage(Log.INFO, "tag", Matchers.equalTo("exact message"));
 * // This adds a 2nd expectation, so 2 log statements with the same value must be present.
 * logged.expectLogMessage(Log.INFO, "tag", Matchers.equalTo("exact message"));
 * }<pre>
 *
 * <p>This means that you may not be able to trivially convert from using one style of expectation
 * to the other. In general it is preferable to match the number of expectations to the number of
 * expected log messages (i.e. using the {@code Matcher} APIs) but some existing tests may rely on
 * the older de-duplication behaviour.
 */
public final class ExpectedLogMessagesRule implements TestRule {
  /** Tags that apps can't prevent. We exempt them globally. */
  private static final ImmutableSet<String> UNPREVENTABLE_TAGS =
      ImmutableSet.of(
          "Typeface",
          "RingtoneManager",
          // When Robolectric's shadow of PowerManager.WakeLock.acquire() was updated to set some
          // bits on the underlying WakeLock object, it started logging a wtf when a WakeLock was
          // still held when it was finalized.
          "PowerManager",
          // Fails when attempting to preload classes by name
          "PhonePolicy",
          // Ignore MultiDex log messages
          "MultiDex",
          // Logged starting with Android 33 as:
          // E/RippleDrawable: The RippleDrawable.STYLE_PATTERNED animation is not supported for a
          // non-hardware accelerated Canvas. Skipping animation.
          "RippleDrawable");

  private final Set<ExpectedLogItem> expectedLogs = new HashSet<>();
  private final Set<LogItem> observedLogs = new HashSet<>();
  private final Set<LogItem> unexpectedErrorLogs = new HashSet<>();
  private final Set<String> expectedTags = new HashSet<>();
  private final Set<String> observedTags = new HashSet<>();

  private boolean shouldIgnoreMissingLoggedTags = false;

  @Override
  public Statement apply(final Statement base, Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        base.evaluate();
        List<LogItem> logs = ShadowLog.getLogs();
        Map<ExpectedLogItem, Boolean> expectedLogItemMap = new HashMap<>();
        for (ExpectedLogItem item : expectedLogs) {
          expectedLogItemMap.put(item, false);
        }
        for (LogItem log : logs) {
          LogItem logItem = new LogItem(log.type, log.tag, log.msg, log.throwable);
          if (updateExpected(logItem, expectedLogItemMap)) {
            observedLogs.add(logItem);
            continue;
          }
          if (log.type >= Log.ERROR) {
            if (UNPREVENTABLE_TAGS.contains(log.tag)) {
              continue;
            }
            if (expectedTags.contains(log.tag)) {
              observedTags.add(log.tag);
              continue;
            }
            unexpectedErrorLogs.add(log);
          }
        }
        if (!unexpectedErrorLogs.isEmpty() || expectedLogItemMap.containsValue(false)) {
          Set<ExpectedLogItem> unobservedLogs = new HashSet<>();
          for (Map.Entry<ExpectedLogItem, Boolean> entry : expectedLogItemMap.entrySet()) {
            if (!entry.getValue()) {
              unobservedLogs.add(entry.getKey());
            }
          }
          throw new AssertionError(
              "Expected and observed logs did not match."
                  + "\nExpected:                   "
                  + expectedLogs
                  + "\nExpected, and observed:     "
                  + observedLogs
                  + "\nExpected, but not observed: "
                  + unobservedLogs
                  + "\nObserved, but not expected: "
                  + unexpectedErrorLogs);
        }
        if (!expectedTags.equals(observedTags) && !shouldIgnoreMissingLoggedTags) {
          throw new AssertionError(
              "Expected and observed tags did not match. "
                  + "Expected tags should not be used to suppress errors, only expect them."
                  + "\nExpected:                   "
                  + expectedTags
                  + "\nExpected, and observed:     "
                  + observedTags
                  + "\nExpected, but not observed: "
                  + Sets.difference(expectedTags, observedTags));
        }
      }
    };
  }

  /**
   * Adds an expected log statement. If this log is not printed during test execution, the test case
   * will fail.
   *
   * <p>This will also match any log statement which contains a throwable as well. For verifying the
   * throwable, please see {@link #expectLogMessageWithThrowable(int, String, Matcher<String>,
   * Matcher<Throwable>)}.
   *
   * <p>Do not use this to suppress failures. Use this to test that expected error cases in your
   * code cause log messages to be printed.
   *
   * <p>See class level documentation for a note about using {@code Matcher}s.
   */
  public void expectLogMessage(int level, String tag, Matcher<String> messageMatcher) {
    expectLog(level, tag, messageMatcher, null);
  }

  /**
   * Adds an expected log statement. If this log is not printed during test execution, the test case
   * will fail.
   *
   * <p>This will also match any log statement which contains a throwable as well. For verifying the
   * throwable, please see {@link #expectLogMessageWithThrowable(int, String, String, Throwable)}.
   *
   * <p>Do not use this to suppress failures. Use this to test that expected error cases in your
   * code cause log messages to be printed.
   */
  public void expectLogMessage(int level, String tag, String message) {
    expectLogMessage(level, tag, MsgEq.of(message));
  }

  /**
   * Adds an expected log statement using a regular expression. If this log is not printed during
   * test execution, the test case will fail. When possible, log output should be made determinstic
   * and {@link #expectLogMessage(int, String, String)} used instead.
   *
   * <p>This will also match any log statement which contain a throwable as well. For verifying the
   * throwable, please see {@link #expectLogMessagePatternWithThrowableMatcher}.
   *
   * <p>Do not use this to suppress failures. Use this to test that expected error cases in your
   * code cause log messages to be printed.
   */
  public void expectLogMessagePattern(int level, String tag, Pattern messagePattern) {
    expectLogMessage(level, tag, MsgRegex.of(messagePattern));
  }

  /**
   * Adds an expected log statement with extra check of {@link Throwable}. If this log is not
   * printed during test execution, the test case will fail. Do not use this to suppress failures.
   * Use this to test that expected error cases in your code cause log messages to be printed.
   *
   * <p>See class level documentation for a note about using {@code Matcher}s.
   */
  public void expectLogMessageWithThrowable(
      int level, String tag, Matcher<String> messagMatcher, Matcher<Throwable> throwableMatcher) {
    expectLog(level, tag, messagMatcher, throwableMatcher);
  }

  /**
   * Adds an expected log statement with extra check of {@link Throwable}. If this log is not
   * printed during test execution, the test case will fail. Do not use this to suppress failures.
   * Use this to test that expected error cases in your code cause log messages to be printed.
   */
  public void expectLogMessageWithThrowable(
      int level, String tag, String message, Throwable throwable) {
    expectLogMessageWithThrowable(level, tag, MsgEq.of(message), equalTo(throwable));
  }

  /**
   * Adds an expected log statement using a regular expression, with an extra check of {@link
   * Matcher<Throwable>}. If this log is not printed during test execution, the test case will fail.
   * When possible, log output should be made deterministic and {@link #expectLogMessage(int,
   * String, String)} used instead.
   *
   * <p>See class level documentation for a note about using {@code Matcher}s.
   */
  public void expectLogMessagePatternWithThrowableMatcher(
      int level, String tag, Pattern messagePattern, Matcher<Throwable> throwableMatcher) {
    expectLogMessageWithThrowable(level, tag, MsgRegex.of(messagePattern), throwableMatcher);
  }

  /**
   * Adds an expected log statement with extra check of {@link Matcher}. If this log is not printed
   * during test execution, the test case will fail. Do not use this to suppress failures. Use this
   * to test that expected error cases in your code cause log messages to be printed.
   *
   * <p>See class level documentation for a note about using {@code Matcher}s.
   */
  public void expectLogMessageWithThrowableMatcher(
      int level, String tag, String message, Matcher<Throwable> throwableMatcher) {
    expectLogMessageWithThrowable(level, tag, MsgEq.of(message), throwableMatcher);
  }

  private void expectLog(
      int level, String tag, Matcher<String> messageMatcher, Matcher<Throwable> throwableMatcher) {
    expectedLogs.add(new ExpectedLogItem(level, tag, messageMatcher, throwableMatcher));
  }

  /**
   * Blanket suppress test failures due to errors from a tag. If this tag is not printed at
   * Log.ERROR during test execution, the test case will fail (unless {@link
   * #ignoreMissingLoggedTags(boolean)} is used).
   *
   * <p>Avoid using this method when possible. Prefer to assert on the presence of a specific
   * message using {@link #expectLogMessage} in test cases that *intentionally* trigger an error.
   */
  public void expectErrorsForTag(String tag) {
    if (UNPREVENTABLE_TAGS.contains(tag)) {
      throw new AssertionError("Tag `" + tag + "` is already suppressed.");
    }
    expectedTags.add(tag);
  }

  /**
   * If set true, tests that call {@link #expectErrorsForTag(String)} but do not log errors for the
   * given tag will not fail. By default this is false.
   *
   * <p>Avoid using this method when possible. Prefer tests that print (or do not print) log
   * messages deterministically.
   */
  public void ignoreMissingLoggedTags(boolean shouldIgnore) {
    shouldIgnoreMissingLoggedTags = shouldIgnore;
  }

  private boolean updateExpected(
      LogItem logItem, Map<ExpectedLogItem, Boolean> expectedLogItemMap) {
    for (ExpectedLogItem expectedLogItem : expectedLogItemMap.keySet()) {
      if (expectedLogItem.type == logItem.type
          && Objects.equals(expectedLogItem.tag, logItem.tag)
          && expectedLogItem.msgMatcher.matches(logItem.msg)
          && matchThrowable(expectedLogItem, logItem.throwable)) {
        expectedLogItemMap.put(expectedLogItem, true);
        return true;
      }
    }

    return false;
  }

  private static boolean matchThrowable(ExpectedLogItem logItem, Throwable throwable) {
    if (logItem.throwableMatcher != null) {
      return logItem.throwableMatcher.matches(throwable);
    }

    // Return true in case no throwable / throwable-matcher were specified.
    return true;
  }

  private static class ExpectedLogItem {
    final int type;
    final String tag;
    // Either or both matches can be null ()
    final Matcher<String> msgMatcher;
    final Matcher<Throwable> throwableMatcher;

    private ExpectedLogItem(
        int type, String tag, Matcher<String> msgMatcher, Matcher<Throwable> throwableMatcher) {
      this.type = type;
      this.tag = tag;
      this.msgMatcher = msgMatcher;
      this.throwableMatcher = throwableMatcher;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }

      if (!(o instanceof ExpectedLogItem)) {
        return false;
      }

      ExpectedLogItem log = (ExpectedLogItem) o;
      return type == log.type
          && !(tag != null ? !tag.equals(log.tag) : log.tag != null)
          && Objects.equals(msgMatcher, log.msgMatcher)
          && Objects.equals(throwableMatcher, log.throwableMatcher);
    }

    @Override
    public int hashCode() {
      return Objects.hash(type, tag, msgMatcher, throwableMatcher);
    }

    @Override
    public String toString() {
      String msgStr = (msgMatcher == null) ? "" : (", msg=" + msgMatcher);
      String throwableStr = (throwableMatcher == null) ? "" : (", throwable=" + throwableMatcher);
      return String.format(
          "ExpectedLogItem{timeString='null', type=%s, tag='%s'%s%s}",
          type, tag, msgStr, throwableStr);
    }
  }

  // Similar to IsEqualTo matcher in Hamcrest, but supports equals/hashCode for de-duplication.
  private static final class MsgEq extends BaseMatcher<String> {
    static Matcher<String> of(String msg) {
      return msg != null ? new MsgEq(msg) : null;
    }

    private final String msg;

    private MsgEq(String msg) {
      this.msg = msg;
    }

    @Override
    public boolean matches(Object other) {
      // Allow direct cast since we only use this matcher in a type-safe way.
      return msg.equals((String) other);
    }

    // Designed to match legacy toString() behaviour - do not modify.
    @Override
    public void describeTo(org.hamcrest.Description description) {
      description.appendText("'" + msg + "'");
    }

    // This matches legacy behaviour to allow ExpectedLogItem to de-duplicate expectations.
    @Override
    public boolean equals(Object matcher) {
      return matcher instanceof MsgEq && msg.equals(((MsgEq) matcher).msg);
    }

    @Override
    public int hashCode() {
      return msg.hashCode() ^ MsgEq.class.hashCode();
    }
  }

  // Similar to MatchesPattern in Hamcrest, but supports equals/hashCode for de-duplication.
  private static final class MsgRegex extends BaseMatcher<String> {
    static Matcher<String> of(Pattern pattern) {
      return pattern != null ? new MsgRegex(pattern) : null;
    }

    private final Pattern pattern;

    private MsgRegex(Pattern pattern) {
      this.pattern = pattern;
    }

    @Override
    public boolean matches(Object other) {
      // Allow direct cast since we only use this matcher in a type-safe way.
      return pattern.matcher((String) other).matches();
    }

    // Designed to match legacy toString() behaviour - do not modify.
    @Override
    public void describeTo(org.hamcrest.Description description) {
      description.appendText("'" + pattern + "'");
    }

    // This matches legacy behaviour to allow ExpectedLogItem to de-duplicate regex expectations.
    @Override
    public boolean equals(Object other) {
      return other instanceof MsgRegex ? isEqual(pattern, ((MsgRegex) other).pattern) : false;
    }

    @Override
    public int hashCode() {
      return Objects.hash(pattern.pattern(), pattern.flags());
    }

    /** Returns true if the pattern and flags compiled in a {@link Pattern} were the same. */
    private static boolean isEqual(Pattern a, Pattern b) {
      return a.pattern().equals(b.pattern()) && a.flags() == b.flags();
    }
  }
}
