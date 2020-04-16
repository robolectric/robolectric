package org.robolectric.junit.rules;

import android.util.Log;
import com.google.common.collect.ImmutableSet;
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
  private final Set<String> expectedTags = new HashSet<>();
  private final Set<String> observedTags = new HashSet<>();

  private boolean shouldIgnoreMissingLoggedTags = false;
  private boolean shouldIgnoreThrowable = false;

  @Override
  public Statement apply(final Statement base, Description description) {
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        base.evaluate();
        List<LogItem> logs = ShadowLog.getLogs();
        for (LogItem log : logs) {
          LogItem throwLessLogItem =
              new LogItem(log.type, log.tag, log.msg, shouldIgnoreThrowable ? null : log.throwable);
          if (expectedLogs.contains(throwLessLogItem)) {
            observedLogs.add(throwLessLogItem);
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
            if (log.throwable != null) {
              throw new AssertionError(
                  "Unexpected error log message: " + log.tag + ": " + log.msg, log.throwable);
            } else {
              throw new AssertionError("Unexpected error log message: " + log.tag + ": " + log.msg);
            }
          }
        }
        if (!expectedLogs.equals(observedLogs)) {
          throw new AssertionError(
              "Some expected logs were not printed."
                  + "\nExpected: "
                  + expectedLogs
                  + "\nObserved: "
                  + observedLogs);
        }
        if (!expectedTags.equals(observedTags) && !shouldIgnoreMissingLoggedTags) {
          throw new AssertionError(
              "Some expected tags were not printed. "
                  + "Expected tags should not be used to suppress errors, only expect them."
                  + "\nExpected: "
                  + expectedTags
                  + "\nObserved: "
                  + observedTags);
        }
        shouldIgnoreThrowable = false;
      }
    };
  }

  /**
   * Adds an expected log statement. If this log is not printed during test execution, the test case
   * will fail. Do not use this to suppress failures. Use this to test that expected error cases in
   * your code cause log messages to be printed.
   */
  public void expectLogMessage(int level, String tag, String message) {
    shouldIgnoreThrowable = true;
    expectLogMessageWithThrowable(level, tag, message, null);
  }

  /**
   * Adds an expected log statement with extra check of {@link Throwable}. If this log is not
   * printed during test execution, the test case will fail. Do not use this to suppress failures.
   * Use this to test that expected error cases in your code cause log messages to be printed.
   */
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
