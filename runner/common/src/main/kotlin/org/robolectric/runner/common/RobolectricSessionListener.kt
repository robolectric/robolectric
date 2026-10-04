package org.robolectric.runner.common

import java.time.Duration
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration

/**
 * Observes what a [RobolectricSession] does, for example to report where the time of a test run
 * goes. The methods are called on the thread that opened or closed the environment.
 */
@ExperimentalRunnerApi
public interface RobolectricSessionListener {
  /**
   * Called when an environment is set up.
   *
   * @param sandboxCreated whether a sandbox had to be created for it, which takes most of the time,
   *   rather than reused
   * @param duration how long opening it took
   */
  public fun environmentOpened(
    configuration: Configuration,
    sandboxCreated: Boolean,
    duration: Duration,
  ) {}

  /**
   * Called when an environment is torn down.
   *
   * @param duration how long closing it took
   */
  public fun environmentClosed(configuration: Configuration, duration: Duration) {}
}
