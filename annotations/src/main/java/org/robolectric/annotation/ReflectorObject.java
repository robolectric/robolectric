package org.robolectric.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Serves to cache the reflector object instance and lower test runtime.
 *
 * <p>For example, <code>@ReflectorObject MyReflector objectReflector</code> is equivalent to
 * calling <code>reflector(MyReflector.class, realObject)</code>.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ReflectorObject {}
