package org.robolectric.bytecode.testing;

import org.robolectric.internal.Instrument;

@SuppressWarnings("UnusedDeclaration")
@Instrument
public class AClassWithMethodReturningInteger {
  public int normalMethodReturningInteger(int intArg) {
    return intArg + 1;
  }
}
