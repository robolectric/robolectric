package org.robolectric.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Parameters with types that can't be resolved at compile time may be annotated @ClassName.
 *
 * <p>Use this annotation when creating shadow methods that contain new Android types in the method
 * signature that do not exist in older SDK levels.
 *
 * <pre>
 * &#64;Implements(FooAndroidClass.class)
 * class ShadowFooAndroidClass {
 *
 *    // A method shadowing FooAndroidClass#setBar(com.android.RealClassName, int, String)
 *    &#64;Implementation
 *    public void setBar(&#64;ClassName("com.android.RealClassName") Object para1, int para2, String para3) {
 *
 *    }
 * }
 * </pre>
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ClassName {

  /**
   * The class name intended for this parameter.
   *
   * <p>Use the value as returned from {@link Class#getName()}, not {@link
   * Class#getCanonicalName()}; e.g. {@code Foo$Bar} instead of {@code Foo.Bar}.
   */
  String value();
}
