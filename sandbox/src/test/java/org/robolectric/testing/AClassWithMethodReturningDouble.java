package org.robolectric.testing;

import org.robolectric.annotation.internal.Instrument;

@SuppressWarnings("UnusedDeclaration")
@Instrument
public class AClassWithMethodReturningDouble {
  public double normalMethodReturningDouble(double doubleArg) {
    return doubleArg + 1;
  }
}
