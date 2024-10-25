package org.robolectric.util.inject;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the annotated type is intended as a replacement for another type.
 *
 * @deprecated Use {@link Supersedes} instead. This annotation is misspelled.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Deprecated
public @interface Supercedes {

  /** The type that is superseded by the annotated type. */
  Class<?> value();
}
