package org.robolectric.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated method is used to reset static state in a shadow.
 *
 * <p>The annotated method should be public static, have no parameters, and be contained in a shadow
 * class annotated with {@link Implements}.
 *
 * <p>The Robolectric test runner will call Resetter annotated methods during test teardown, if the
 * shadow class or its targeted Implements class has been loaded by the ClassLoader.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Resetter {}
