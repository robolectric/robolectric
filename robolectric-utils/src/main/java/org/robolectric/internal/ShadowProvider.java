package org.robolectric.internal;

import java.util.Map;

/**
 * Interface implemented by packages that provide shadows to Robolectric.
 */
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
   * @return  Shadow mapping.
   */
  Map<String, String> getShadowMap();

  enum Tier {Base, Custom}

  /**
   * Return the tier of shadows of this Shadow Provider. By default base shadows included with
   * Robolectric have the Base tier.
   *
   * @return  The tier.
   */
  Tier getTier();
}
