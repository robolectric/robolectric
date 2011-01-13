package com.xtremelabs.robolectric.internal;

/**
 * Indicates that a class should not be stripped/instrumented under any circumstances.
 */
@java.lang.annotation.Documented
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE})
public @interface DoNotInstrument {
}
