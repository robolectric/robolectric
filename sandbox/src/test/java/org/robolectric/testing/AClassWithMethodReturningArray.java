package org.robolectric.testing;

import org.robolectric.annotation.internal.Instrument;

@SuppressWarnings("UnusedDeclaration")
@Instrument
public class AClassWithMethodReturningArray {
  public String[] normalMethodReturningArray() {
    return new String[] { "hello, working!" };
  }
}
