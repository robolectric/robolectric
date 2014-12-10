package org.robolectric.annotation;

/**
 * Indicates that the annotated method is used to reset static state in a shadow.
 */
@java.lang.annotation.Documented
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({java.lang.annotation.ElementType.METHOD})
public @interface Resetter {
}
