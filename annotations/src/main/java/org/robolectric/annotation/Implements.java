package org.robolectric.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.robolectric.shadow.api.ShadowPicker;

/**
 * Indicates that a class declaration is intended to shadow an Android class declaration. The
 * Robolectric runtime searches classes with this annotation for methods with the {@link
 * Implementation} annotation and calls them in place of the methods on the Android class.
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
   * @deprecated Use the {@link org.robolectric.annotation.ClassName} annotation or the {@link
   *     org.robolectric.annotation.Implementation#methodName()} annotation parameter instead.
   * @return True to disable strict signature matching.
   */
  @Deprecated
  boolean looseSignatures() default false;

  /** If specified, the shadow class will be applied only for this SDK or greater. */
  int minSdk() default -1;

  /** If specified, the shadow class will be applied only for this SDK or lesser. */
  int maxSdk() default -1;

  /**
   * If specified, the {@code picker} will be instantiated and called from within the newly-created
   * Robolectric classloader. All shadow classes implementing the same Android class must use the
   * same {@link ShadowPicker}.
   */
  Class<? extends ShadowPicker<?>> shadowPicker() default DefaultShadowPicker.class;

  /**
   * If set to true, Robolectric will invoke the native method variant instead of the no-op variant.
   * This requires the native method to be bound, or an {@link UnsatisfiedLinkError} will occur.
   *
   * <p>This method has precedence over {@link Implements#callThroughByDefault()}. For instance, if
   * both this method and {@link Implements#callThroughByDefault()} are true, the native method
   * variant will be preferred over the no-op native variant.
   */
  boolean callNativeMethodsByDefault() default false;

  /**
   * An interface used as the default for the {@code picker} param. Indicates that no custom {@link
   * ShadowPicker} is being used.
   */
  interface DefaultShadowPicker extends ShadowPicker<Object> {}
}
