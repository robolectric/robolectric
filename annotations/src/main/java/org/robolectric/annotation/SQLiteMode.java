package org.robolectric.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A {@link org.robolectric.pluginapi.config.Configurer} annotation for controlling which SQLite
 * shadow implementation is used for the {@link android.database} package.
 *
 * @deprecated This annotation will be deleted in a forthcoming Robolectric release.
 */
@Documented
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD})
public @interface SQLiteMode {

  /**
   * Specifies the different supported SQLite modes.
   *
   * @deprecated This enum is deprecated along with {@link SQLiteMode}. The default behavior is now
   *     equivalent to {@link SQLiteMode.Mode#NATIVE} mode, so this annotation is generally no
   *     longer needed.
   */
  @Deprecated
  enum Mode {
    /**
     * Use the legacy SQLite implementation backed by sqlite4java.
     *
     * @deprecated This mode is obsolete and will be removed soon.
     */
    @Deprecated
    LEGACY,
    /**
     * Use the new SQLite implementation backed by native Android code from AOSP.
     *
     * @deprecated {@code NATIVE} is the default mode and does not need to be stated explicitly.
     */
    NATIVE,
  }

  Mode value();
}
