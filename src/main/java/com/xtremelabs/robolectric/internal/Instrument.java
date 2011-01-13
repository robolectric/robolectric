package com.xtremelabs.robolectric.internal;

/**
 * Indicates that a class should always be instrumented by AndroidTranslator regardless of its package.
 */
@java.lang.annotation.Documented
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE})
public @interface Instrument {
}
