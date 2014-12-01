package org.robolectric.annotation;

/**
 * Indicates that a class declaration is intended to Shadow an Android class declaration. The Robolectric runtime
 * searches classes with this annotation for methods with the {@link Implementation} annotation and calls them in
 * place of the methods on the Android class.
 */
@java.lang.annotation.Documented
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE})
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
   * If true, Robolectric will invoke the actual Android code for any method that isn't shadowed.
   *
   * @return True to invoke the underlying method.
   */
  boolean callThroughByDefault() default true;

  /**
   * If true, Robolectric will invoke @Implementation methods from superclasses.
   *
   * @return True to invoke superclass methods.
   */
  boolean inheritImplementationMethods() default false;

  /**
   * If true, when an exact method signature match isn't found, Robolectric will look for a method
   * with the same name but with all argument types replaced with java.lang.Object.
   *
   * @return True to disable strict signature matching.
   */
  boolean looseSignatures() default false;
}
