package org.robolectric.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A {@link org.robolectric.pluginapi.config.Configurer} annotation for controlling Robolectric's
 * Looper behavior.
 *
 * <p>Note that LooperMode can also be controlled via the `robolectric.looperMode` system
 * property.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD})
public @interface LooperMode {

  /**
   * Specifies the different supported Looper modes.
   */
  enum Mode {
    /**
     * Robolectric's mode prior to 4.3. Uses the {@link org.robolectric.util.Scheduler} API
     * TODO add more docs
     */
    LEGACY,
    /**
     * All posted tasks are queued, and need to be explicitly executed.
     * TODO add more docs
     */
    PAUSED,
    /**
     * Currently not supported.
     *
     * In future, will have free running threads etc
     */
    RUNNING
  }

  /**
   * Set the Looper mode.
   */
  Mode value();
}