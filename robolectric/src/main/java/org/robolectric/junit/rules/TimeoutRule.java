package org.robolectric.junit.rules;

import java.util.concurrent.TimeUnit;
import javax.annotation.Nonnull;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.robolectric.internal.TimeLimitedStatement;

/**
 * Robolectric's replacement for JUnit's {@link org.junit.rules.Timeout Timeout}.
 *
 * <p>{@link org.junit.rules.Timeout Timeout} spawns a new thread, which is not compatible with
 * Robolectric's Scheduler. Instead this Rule uses {@link
 * org.robolectric.internal.TimeLimitedStatement TimeLimitedStatement} like {@code @Test(timeout=)}
 * does.
 *
 * <p>Example usage:
 *
 * <pre>
 * {@literal @}Rule public final TimeoutRule timeoutRule = TimeoutRule.seconds(40);
 *
 * {@literal @}Test
 *  public void testWhichShouldFinishIn40Seconds() {
 *    // ...
 *  }
 * </pre>
 */
public class TimeoutRule implements TestRule {

  private final long timeout;
  @Nonnull private final TimeUnit timeUnit;

  /**
   * Create a {@code TimeoutRule} instance with the timeout specified at the timeUnit of granularity
   * of the provided {@code TimeUnit}.
   *
   * @param timeout the maximum time to allow the test to run before it should timeout
   * @param timeUnit the time unit for the {@code timeout}
   */
  public TimeoutRule(long timeout, @Nonnull TimeUnit timeUnit) {
    this.timeout = timeout;
    this.timeUnit = timeUnit;
  }

  @Override
  public Statement apply(Statement base, Description description) {
    return new TimeLimitedStatement(timeUnit.toMillis(timeout), base);
  }

  /**
   * Creates a {@link org.robolectric.junit.rules.TimeoutRule TimeoutRule} that will timeout a test
   * after the given duration, in milliseconds.
   */
  public static TimeoutRule millis(long millis) {
    return new TimeoutRule(millis, TimeUnit.MILLISECONDS);
  }

  /**
   * Creates a {@link org.robolectric.junit.rules.TimeoutRule TimeoutRule} that will timeout a test
   * after the given duration, in seconds.
   */
  public static TimeoutRule seconds(long seconds) {
    return new TimeoutRule(seconds, TimeUnit.SECONDS);
  }
}
