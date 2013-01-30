package com.xtremelabs.robolectric.internal;


/**
 * Indicates that a class declaration is intended to Shadow an Android class declaration. The Robolectric runtime
 * searches classes with this annotation for methods with the {@link Implementation} annotation and calls them in
 * place of the methods on the Android class.
 */
@java.lang.annotation.Documented
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({java.lang.annotation.ElementType.TYPE})
public @interface Implements {
    /**
     * the Android class to be shadowed
     */
    Class value();
}
