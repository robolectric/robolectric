package org.robolectric.bytecode.testing;

import org.robolectric.internal.Instrument;

@SuppressWarnings("UnusedDeclaration")
@Instrument
public class AClassWithStaticMethod {
  public static String staticMethod(String stringArg) {
    return "staticMethod(" + stringArg + ")";
  }
}
