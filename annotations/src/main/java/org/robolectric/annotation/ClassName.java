package org.robolectric.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Parameters, function return with types that can't be resolved at compile time may be annotated
 * with @ClassName.
 *
 * <p>Use this annotation when creating shadow methods that contain new Android types in the method
 * signature, return type that do not exist in older SDK levels.
 *
 * <pre>
 * &#64;Implements(FooAndroidClass.class)
 * class ShadowFooAndroidClass {
 *
 *    // A method shadowing FooAndroidClass#setBar(com.android.RealClassName, int, String)
 *    // Generally, &#64;ClassName will be used together with Object type.
 *    &#64;Implementation
 *    public &#64;ClassName("com.android.RealReturnType") Object setBar(
 *        &#64;ClassName("com.android.RealClassName") Object para1,
 *        int para2,
 *        String para3) {
 *
 *    }
 * }
 * </pre>
 */
@Target({ElementType.TYPE_USE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ClassName {

  /**
   * The class name intended for the parameter or the function return value.
   *
   * <p>Use the value as returned from {@link Class#getName()}, not {@link
   * Class#getCanonicalName()}; e.g. {@code Foo$Bar} instead of {@code Foo.Bar}.
   */
  String value();
}
