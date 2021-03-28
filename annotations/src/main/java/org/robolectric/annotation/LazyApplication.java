package org.robolectric.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A {@link org.robolectric.pluginapi.config.Configurer} annotation that dictates whether or not
 * Robolectric should lazily instantiate the Application under test (as well as the test
 * Instrumentation).
 *
 * <p>In particular, any test with {@link LazyLoad.ON} that does not need either of the Application
 * or the Instrumentation will load neither (and recoup the associated cost)
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
