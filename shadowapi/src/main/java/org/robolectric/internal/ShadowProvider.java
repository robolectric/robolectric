package org.robolectric.internal;

import java.util.Collections;
import java.util.Map;

/**
 * Interface implemented by packages that provide shadows to Robolectric.
 */
@SuppressWarnings("NewApi")
public interface ShadowProvider {

  /**
   * Reset the static state of all shadows provided by this package.
   */
  void reset();

  /**
   * Array of Java package names that are shadowed by this package.
   *
   * @return  Array of Java package names.
   */
  String[] getProvidedPackageNames();

  /**
   * Return the mapping of class name to shadow name.
   *
   * @return Shadow mapping.
   */
  Map<String, String> getShadowMap();

  /**
   * Map of framework classes which may be represented by more than one shadow, to be picked
   * at runtime.
   *
   * @return A map from the name of the framework class to the name of its
   *     {#link org.robolectric.shadow.apiShadowPicker}.
   */
  default Map<String, String> getShadowPickerMap() {
    return Collections.emptyMap();
  }
}
