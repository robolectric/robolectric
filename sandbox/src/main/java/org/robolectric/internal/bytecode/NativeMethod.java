package org.robolectric.internal.bytecode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Added to instrumented methods to indicate that the original was a native method. This can be used
 * by tools that do further processing on Robolectric instrumented classes related to native
 * methods.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface NativeMethod {}
