package org.robolectric.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A {@link org.robolectric.pluginapi.config.Configurer} annotation for controlling which graphics
 * shadow implementation is used for the {@link android.graphics} package.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD})
public @interface GraphicsMode {

  /** Specifies the different supported graphics modes. */
  enum Mode {
    /** Use legacy graphics shadows that are no-ops and fakes. */
    LEGACY,
    /** Use graphics shadows libraries backed by native Android graphics code. */
    NATIVE,
  }

  Mode value();
}
