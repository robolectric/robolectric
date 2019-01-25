package org.robolectric.pluginapi;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Marks a component of Robolectric that may be replaced with a custom implementation.
 */
@Documented
@Target(ElementType.TYPE)
public @interface ExtensionPoint {

}
