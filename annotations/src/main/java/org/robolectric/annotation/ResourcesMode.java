package org.robolectric.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A {@link org.robolectric.pluginapi.config.Configurer} annotation for controlling Robolectric's
 * android resource implementation.
 *
 * <p>Currently Robolectric will default to {@link ResourcesMode.Mode#BINARY} , but this can be
 * overridden by applying a @ResourcesMode(NewMode) annotation to a test package, test class, or
 * test method, or via the 'robolectric.resourcesMode' system property.
 *
 * @see org.robolectric.plugins.ResourceModeConfigurer
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD})
public @interface ResourcesMode {

  enum Mode {
    /**
     * Default: Reads binary resource data (arsc) using a Java implementation, manually
     * transliterated from native code
     */
    BINARY,
    /**
     * Experimental: use AOSP native code to read resource data. Currently only functioning on
     * android V and linux
     */
    NATIVE,
  }

  /** Set the Resources mode. */
  ResourcesMode.Mode value();
}
