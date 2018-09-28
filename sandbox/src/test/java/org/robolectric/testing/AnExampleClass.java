package org.robolectric.testing;

import org.robolectric.annotation.internal.Instrument;

@SuppressWarnings("UnusedDeclaration")
@Instrument
public class AnExampleClass {
  static int foo = 123;

  public static final String STATIC_FINAL_FIELD = "STATIC_FINAL_FIELD";
  public final String nonstaticFinalField = "nonstaticFinalField";

  public String normalMethod(String stringArg, int intArg) {
    return "normalMethod(" + stringArg + ", " + intArg + ")";
  }

  //        abstract void abstractMethod(); todo
}
