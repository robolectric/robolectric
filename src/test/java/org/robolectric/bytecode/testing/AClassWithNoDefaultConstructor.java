package org.robolectric.bytecode.testing;

import org.robolectric.internal.Instrument;

@SuppressWarnings("UnusedDeclaration")
@Instrument public class AClassWithNoDefaultConstructor {
  private String name;

  AClassWithNoDefaultConstructor(String name) {
    this.name = name;
  }
}
