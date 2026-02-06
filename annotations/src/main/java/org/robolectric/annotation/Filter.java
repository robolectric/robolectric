package org.robolectric.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.jspecify.annotations.Nullable;

/**
 * Indicates that a method declaration is intended to be called after the original method with the
 * same signature on the associated Android class.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Filter {
  int DEFAULT_SDK = -1;

  /** The annotated filter method will be invoked only for the specified SDK or greater. */
  int minSdk() default DEFAULT_SDK;

  /** The annotated filter method will be invoked only for the specified SDK or lesser. */
  int maxSdk() default DEFAULT_SDK;

  /**
   * The filtered method name.
   *
   * @return The expected filtered method name. If it is empty/null, the Robolectric will use the
   *     method's name that is marked by @Filter as the filtered method name.
   */
  @Nullable String methodName() default "";
}
