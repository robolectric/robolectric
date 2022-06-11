package org.robolectric.internal;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

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
   * Return a collection of Map.Entry objects representing the mapping of class name to shadow name.
   *
   * <p>This is a multimap instead of a regular map in order to support, for instance, multiple
   * shadows per class that only differ by SDK level.
   *
   * <p>It also uses a {@code Collection<Entry<String, String>>} as the return value to avoid having
   * a dependency on something like Guava Multimap.
   *
   * @return Shadow mapping.
   */
  Collection<Entry<String, String>> getShadows();

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
