package org.robolectric.internal;

/**
 * Indicates that the annotated method is hidden in the public Android API, so RobolectricWiringTest shouldn't complain.
 */
@java.lang.annotation.Documented
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({java.lang.annotation.ElementType.METHOD})
public @interface HiddenApi {
}
