package com.xtremelabs.robolectric.annotation;

/**
 * Indicate that roboletric should look for values that is specific for the locale
 * 
 */
@java.lang.annotation.Documented
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(java.lang.annotation.ElementType.METHOD)
public @interface Values {
	String locale();
}
