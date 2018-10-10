package org.robolectric.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.robolectric.shadow.api.ShadowPicker;

/**
 * Indicates that a class declaration is intended to shadow an Android class declaration.
 * The Robolectric runtime searches classes with this annotation for methods with the
 * {@link Implementation} annotation and calls them in place of the methods on the Android
 * class.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface Implements {

  /**
   * The Android class to be shadowed.
   *
   * @return Android class to shadow.
   */
  Class<?> value() default void.class;

  /**
   * Android class name (if the Class object is not accessible).
   *
   * @return Android class name.
   */
  String className() default "";

  /**
   * Denotes that this type exists in the public Android SDK. When this value is true, the
   * annotation processor will generate a shadowOf method.
   *
   * @return True if the type is exposed in the Android SDK.
   */
  boolean isInAndroidSdk() default true;

  /**
   * If true, Robolectric will invoke the actual Android code for any method that isn't shadowed.
   *
   * @return True to invoke the underlying method.
   */
  boolean callThroughByDefault() default true;

  /**
   * If true, when an exact method signature match isn't found, Robolectric will look for a method
   * with the same name but with all argument types replaced with java.lang.Object.
   *
   * @return True to disable strict signature matching.
   */
  boolean looseSignatures() default false;

  /**
   * If specified, the shadow class will be applied only for this SDK or greater.
   */
  int minSdk() default -1;

  /**
   * If specified, the shadow class will be applied only for this SDK or lesser.
   */
  int maxSdk() default -1;

  /**
   * If specified, the `picker` will be instantiated and called from within the newly-created
   * Robolectric classloader. All shadow classes implementing the same Android class must use
   * the same {@link ShadowPicker}.
   */
  Class<? extends ShadowPicker<?>> shadowPicker() default DefaultShadowPicker.class;

  interface DefaultShadowPicker extends ShadowPicker<Object> {
  }
}
