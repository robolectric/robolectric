package org.robolectric.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jspecify.annotations.NonNull;

/**
 * A {@link org.robolectric.pluginapi.config.Configurer} annotation for controlling how Robolectric
 * performs UI layout.
 *
 * <p>PR <a href="https://github.com/robolectric/robolectric/pull/4818">#4818</a> changed
 * Robolectric to be more realistic when performing layout on Android views. This change in behavior
 * could cause tests still using the legacy {@link LooperMode.Mode#LEGACY LEGACY} looper mode or
 * relying on views being a specific size to fail.
 *
 * <p>This annotation can be applied to tests to have Robolectric perform the legacy, less accurate
 * mechanism of laying out and measuring Android text views, as a stopgap until the tests can be
 * properly fixed.
 *
 * @deprecated This annotation will be deleted in a forthcoming Robolectric release.
 */
@Documented
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD})
public @interface TextLayoutMode {

  /**
   * Specifies the different supported Text layout modes.
   *
   * @deprecated This enum is deprecated along with {@link TextLayoutMode}. The default behavior is
   *     now equivalent to {@link Mode#REALISTIC} mode, so this annotation is generally no longer
   *     needed.
   */
  @Deprecated
  enum Mode {
    /**
     * Robolectric's layout mode prior to 4.3.
     *
     * @deprecated {@code LEGACY} mode is inaccurate, has known bugs and will be removed in a future
     *     release.
     */
    @Deprecated
    LEGACY,
    /**
     * The new, more accurate layout mechanism.
     *
     * @deprecated {@code REALISTIC} is the default mode and does not need to be stated explicitly.
     */
    @Deprecated
    REALISTIC,
  }

  @NonNull Mode value();

  /**
   * Optional string for storing the issue / bug id tracking the fixing of the affected tests and
   * thus removal of this annotation.
   */
  @NonNull String issueId() default "";
}
