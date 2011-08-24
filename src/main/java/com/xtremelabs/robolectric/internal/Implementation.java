package com.xtremelabs.robolectric.internal;

/**
 * Indicates that a method declaration is intended to Shadow a method with the same signature on the associated
 * Android class.
 *
 * @see Implements
 */
@java.lang.annotation.Documented
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({java.lang.annotation.ElementType.METHOD})
public @interface Implementation {
	boolean i18nSafe() default true;
}
