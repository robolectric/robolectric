package org.robolectric.bytecode.testing;

import org.robolectric.internal.Instrument;

@SuppressWarnings("UnusedDeclaration")
@Instrument
public class AClassWithMethodReturningBoolean {
  public boolean normalMethodReturningBoolean(boolean boolArg, boolean[] boolArrayArg) {
    return true;
  }
}
