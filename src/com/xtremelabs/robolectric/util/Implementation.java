package com.xtremelabs.robolectric.util;

/**
 * Indicates that a method declaration is intended to Shadow a method with the same signature on the associated
 * Android class.
 *
 * @see Implements
 */
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({java.lang.annotation.ElementType.METHOD})
public @interface Implementation {
}
