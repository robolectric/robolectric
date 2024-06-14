package org.robolectric.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a method declaration is intended to shadow a method with the same signature on the
 * associated Android class.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Implementation {
  int DEFAULT_SDK = -1;

  /** The annotated shadow method will be invoked only for the specified SDK or greater. */
  int minSdk() default DEFAULT_SDK;

  /** The annotated shadow method will be invoked only for the specified SDK or lesser. */
  int maxSdk() default DEFAULT_SDK;

  /**
   * The implemented method name.
   *
   * <p>Sometimes internal methods return different types for different SDKs. It's safe because
   * these methods are internal/private methods, not public methods. To support different return
   * types of a method for different SDKs, we often use looseSignature method, although all return
   * types are common types like bool and int. This field/property can be used to fix this issue by
   * using different real methods for different SDKs.
   *
   * @return The expected implemented method name. If it is empty/null, the Robolectric will uses
   *     the method's name that marked by @Implementation as the implemented method name.
   */
  String methodName() default "";
}
