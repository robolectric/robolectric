package org.robolectric.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A {@link org.robolectric.pluginapi.config.Configurer} annotation for controlling which SQLite
 * shadow implementation is used for the {@link android.database} package.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD})
public @interface SQLiteMode {

  /** Specifies the different supported SQLite modes. */
  enum Mode {
    /** Use the legacy SQLite implementation backed by sqlite4java. */
    LEGACY,
    /** Use the new SQLite implementation backed by native Android code from AOSP. */
    NATIVE,
  }

  Mode value();
}
