package org.robolectric.internal;

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
}
