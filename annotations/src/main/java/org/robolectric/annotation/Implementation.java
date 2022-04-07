package org.robolectric.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a method declaration is intended to shadow a method with the same signature
 * on the associated Android class.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Implementation {
  int DEFAULT_SDK = -1;

  /**
   * The annotated shadow method will be invoked only for the specified SDK or greater.
   */
  int minSdk() default DEFAULT_SDK;

  /**
   * The annotated shadow method will be invoked only for the specified SDK or lesser.
   */
  int maxSdk() default DEFAULT_SDK;
}
