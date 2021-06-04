package org.robolectric.annotation.experimental;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A {@link org.robolectric.pluginapi.config.Configurer} annotation that dictates whether or not
 * Robolectric should lazily instantiate the Application under test.
 *
 * <p>In particular, any test with {@link LazyLoad.ON} that does not need the Application will not
 * load it (and recoup the associated cost)
 *
 * <p>NOTE: This feature is currently still experimental, so any users of {@link LazyLoad.ON} do so
 * at their own risk
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD})
public @interface LazyApplication {

  /** Whether or not the Application should be lazily loaded */
  LazyLoad value();

  /** Whether or not the Application should be lazily loaded */
  enum LazyLoad {
    ON,
    OFF,
  }
}
