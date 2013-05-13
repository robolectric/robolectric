package org.robolectric.bytecode.testing;

import org.robolectric.internal.Instrument;

@SuppressWarnings("UnusedDeclaration")
@Instrument
public class AClassWithMethodReturningArray {
  public String[] normalMethodReturningArray() {
    return new String[] { "hello, working!" };
  }
}
