package org.robolectric.internal.bytecode;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configuration settings that can be used on a per-class or per-test basis.
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface SandboxConfig {
  /**
   * A list of shadow classes to enable, in addition to those that are already present.
   *
   * @return A list of additional shadow classes to enable.
   */
  Class<?>[] shadows() default {};  // DEFAULT_SHADOWS

  /**
   * A list of instrumented packages, in addition to those that are already instrumented.
   *
   * @return A list of additional instrumented packages.
   */
  String[] instrumentedPackages() default {};  // DEFAULT_INSTRUMENTED_PACKAGES
}
