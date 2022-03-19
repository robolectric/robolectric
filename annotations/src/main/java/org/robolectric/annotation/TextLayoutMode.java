package org.robolectric.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A {@link org.robolectric.pluginapi.config.Configurer} annotation for controlling how Robolectric
 * performs UI layout.
 *
 * <p>PR #4818 changed Robolectric to be more realistic when performing layout on Android views.
 * This change in behavior could cause tests still using the legacy 'UNPAUSED' looper mode or
 * relying on views being a specific size to fail.
 *
 * <p>This annotation can be applied to tests to have Robolectric perform the legacy, less accurate
 * mechanism of laying out and measuring Android text views, as a stopgap until the tests can be
 * properly fixed.
 *
 * <p>This annotation will be deleted in a forthcoming Robolectric release.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD})
public @interface TextLayoutMode {

  /** Specifies the different supported Text layout modes. */
  enum Mode {
    /**
     * Robolectric's layout mode prior to 4.3.
     *
     * @deprecated LEGACY mode is inaccurate, has known bugs and will be removed in a future
     *     release.
     */
    @Deprecated
    LEGACY,
    /**
     * The new, more accurate layout mechanism.
     *
     * @deprecated REALTISTIC is the default mode and does not need to be stated explicity.
     */
    @Deprecated
    REALISTIC,
  }

  Mode value();

  /**
   * Optional string for storing the issue / bug id tracking the fixing of the affected tests and
   * thus removal of this annotation.
   */
  String issueId() default "";
}
