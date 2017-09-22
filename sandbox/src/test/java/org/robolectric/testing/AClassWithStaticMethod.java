package org.robolectric.testing;

import org.robolectric.annotation.internal.Instrument;

@SuppressWarnings("UnusedDeclaration")
@Instrument
public class AClassWithStaticMethod {
  public static String staticMethod(String stringArg) {
    return "staticMethod(" + stringArg + ")";
  }
}
