package org.robolectric.util.inject;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated type will be used as a factory. The type must be an interface or
 * {@link Injector} will throw an exception.
 *
 * {@link Injector} will inject an object implementing the annotated interface. When a method on
 * the interface is called, a scoped injector will be created, any parameters passed to the method
 * will be explicitly bound, and an implementation of the method's return type will be computed and
 * returned.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoFactory {

}
