package org.robolectric.internal;

/**
 * @deprecated Use {@link org.robolectric.shadow.api.Shadow#extract(Object)} instead.
 */
@Deprecated
public class ShadowExtractor {
  /**
   * @deprecated Use {@link org.robolectric.shadow.api.Shadow#extract(Object)} instead.
   */
  @Deprecated
  public static Object extract(Object instance) {
    return org.robolectric.shadow.api.Shadow.extract(instance);
  }
}
