package org.robolectric.res;

/**
 * Utility class to that checks if a resource ID is a framework resource or application resource.
 */
public class ResourceIds {
  public static boolean isFrameworkResource(int resId) {
    return ((resId >>> 24) == 0x1);
  }
}