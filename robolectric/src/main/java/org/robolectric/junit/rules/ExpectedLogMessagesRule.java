package org.robolectric.junit.rules;

import android.util.Log;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.ShadowLog.LogItem;

/**
 * Allows tests to assert about the presence of log messages, and turns logged errors that are not
 * explicitly expected into test failures.
 */
public final class ExpectedLogMessagesRule implements TestRule {
  /** Tags that apps can't prevent. We whitelist them globally. */
  private static final ImmutableSet<String> UNPREVENTABLE_TAGS =
      ImmutableSet.of("Typeface", "RingtoneManager");

  private final Set<LogItem> expectedLogs = new HashSet<>();
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
        for (LogItem log : logs) {
          // Figure out observed logs by comparing logs with and without throwable with the expected
          // logs. This handles both expectLogMessage and expectLogMessageWithThrowable.
          LogItem logItem = new LogItem(log.type, log.tag, log.msg, log.throwable);
          LogItem throwLessLogItem = new LogItem(log.type, log.tag, log.msg, null);
          if (expectedLogs.contains(logItem) || expectedLogs.contains(throwLessLogItem)) {
            observedLogs.add(expectedLogs.contains(logItem) ? logItem : throwLessLogItem);
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
        if (!unexpectedErrorLogs.isEmpty() || !expectedLogs.equals(observedLogs)) {
          throw new AssertionError(
              "Expected and observed logs did not match."
                  + "\nExpected:                   "
                  + expectedLogs
                  + "\nExpected, and observed:     "
                  + observedLogs
                  + "\nExpected, but not observed: "
                  + Sets.difference(expectedLogs, observedLogs)
                  + "\nObserved, but not expected: "
                  + Sets.difference(unexpectedErrorLogs, expectedLogs));
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
   * will fail. This will also match any log statement which contain a throwable as well. For
   * verifying the throwable, please see
   * {@link #expectLogMessageWithThrowable(int, String, String, Throwable)}.
   * Do not use this to suppress failures. Use this to test that expected error cases in
   * your code cause log messages to be printed.
   */
  public void expectLogMessage(int level, String tag, String message) {
    expectLogMessageWithThrowable(level, tag, message, null);
  }

  /**
   * Adds an expected log statement with extra check of {@link Throwable}. If this log is not
   * printed during test execution, the test case will fail. Do not use this to suppress failures.
   * Use this to test that expected error cases in your code cause log messages to be printed.
   */
  // TODO(b/156502418): Add matcher instead of throwable as it would be impossible for the test to
  // exactly recreate a throwable that production code throws in many possible cases.
  public void expectLogMessageWithThrowable(
      int level, String tag, String message, Throwable throwable) {
    checkTag(tag);
    expectedLogs.add(new LogItem(level, tag, message, throwable));
  }

  /**
   * Blanket suppress test failures due to errors from a tag. If this tag is not printed at
   * Log.ERROR during test execution, the test case will fail (unless {@link
   * #ignoreMissingLoggedTags()} is used).
   *
   * <p>Avoid using this method when possible. Prefer to assert on the presence of a specific
   * message using {@link #expectLogMessage} in test cases that *intentionally* trigger an error.
   */
  public void expectErrorsForTag(String tag) {
    checkTag(tag);
    if (UNPREVENTABLE_TAGS.contains(tag)) {
      throw new AssertionError("Tag `" + tag + "` is already suppressed.");
    }
    expectedTags.add(tag);
  }

  /**
   * If set true, tests that call {@link #expectErrorsForTag()} but do not log errors for the given
   * tag will not fail. By default this is false.
   *
   * <p>Avoid using this method when possible. Prefer tests that print (or do not print) log
   * messages deterministically.
   */
  public void ignoreMissingLoggedTags(boolean shouldIgnore) {
    shouldIgnoreMissingLoggedTags = shouldIgnore;
  }

  private void checkTag(String tag) {
    if (tag.length() > 23) {
      throw new IllegalArgumentException("Tag length cannot exceed 23 characters: " + tag);
    }
  }
}
