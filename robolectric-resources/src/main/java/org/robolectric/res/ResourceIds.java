package org.robolectric.res;

/**
 * Resource IDs are defined in the following format:-
 *
 * 0xPPTTEEEE
 *
 * Where:
 *
 * PP   = Package, 01 = Framework, 7F = App
 * TT   = Type
 * EEEE = Entry
 */
public class ResourceIds {

  /**
   * Returns true if a resource ID is a Framework ID, false otherwise.
   */
  public static boolean isFrameworkResource(int resId) {
    return (resId & 0xFF000000) == 0x01000000;
  }
}
