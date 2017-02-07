package org.robolectric.testing;

import org.robolectric.annotation.internal.Instrument;

@SuppressWarnings("UnusedDeclaration")
@Instrument public class AClassWithNoDefaultConstructor {
  private String name;

  AClassWithNoDefaultConstructor(String name) {
    this.name = name;
  }
}
