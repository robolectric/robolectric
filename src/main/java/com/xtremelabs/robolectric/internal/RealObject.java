package com.xtremelabs.robolectric.internal;

/**
 * Shadow fields annotated @RealObject will have the real instance injected.
 *
 * @see Implements
 */
@java.lang.annotation.Documented
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({java.lang.annotation.ElementType.FIELD})
public @interface RealObject {
}
