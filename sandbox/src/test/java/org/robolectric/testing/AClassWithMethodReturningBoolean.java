package org.robolectric.testing;

import org.robolectric.annotation.internal.Instrument;

@SuppressWarnings("UnusedDeclaration")
@Instrument
public class AClassWithMethodReturningBoolean {
  public boolean normalMethodReturningBoolean(boolean boolArg, boolean[] boolArrayArg) {
    return true;
  }
}
