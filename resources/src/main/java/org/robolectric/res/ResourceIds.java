package org.robolectric.res;

/**
 * Utility class to that checks if a resource ID is a framework resource or application resource.
 */
public class ResourceIds {
  public static boolean isFrameworkResource(int resId) {
    return ((resId >>> 24) == 0x1);
  }

  public static int getPackageIdentifier(int resId) {
    return (resId >>> 24);
  }

  public static int getTypeIdentifier(int resId) {
    return (resId & 0x00FF0000) >>> 16;
  }

  public static int getEntryIdentifier(int resId) {
    return resId & 0x0000FFFF;
  }

  public static int makeIdentifer(int packageIdentifier, int typeIdentifier, int entryIdenifier) {
    return packageIdentifier << 24 | typeIdentifier << 16 | entryIdenifier;
  }
}