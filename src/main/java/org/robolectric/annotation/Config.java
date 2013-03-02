package org.robolectric.annotation;

/**
 * Indicate that robolectric should look for values that is specific by those qualifiers
 */
@java.lang.annotation.Documented
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(java.lang.annotation.ElementType.METHOD)
public @interface Config {

    /**
     * qualifiers for the values folder, such as "normal-hdpi-16"
     */
    String qualifiers() default "";

    Class<?>[] shadows() default {};

    int sdk() default -1;

    int reportSdk() default -1;
}
