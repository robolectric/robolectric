package org.robolectric.annotation;

/**
 * Indicate that robolectric should look for values that is specific by those qualifiers
 */
@java.lang.annotation.Documented
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(java.lang.annotation.ElementType.METHOD)
public @interface Config {

    /**
     * The Android SDK level to emulate. If not specified, Robolectric defaults to the targetSdkVersion in your app's manifest.
     */
    int emulateSdk() default -1;

    /**
     * Qualifiers for the resource resolution, such as "fr-normal-port-hdpi".
     */
    String qualifiers() default "";

    /**
     * The Android SDK level to report in Build.VERSION.SDK_INT.
     */
    int reportSdk() default -1;

    /**
     * A list of shadow classes to enable, in addition to those that are already present.
     */
    Class<?>[] shadows() default {};
}
