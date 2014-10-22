package org.robolectric.bytecode;

public class RobolectricInternals {

  public static ShadowWrangler getClassHandler() {
    return new ShadowWrangler();
  }
}
