package org.robolectric.testing;

import org.robolectric.annotation.internal.Instrument;

@Instrument
public class Foo {
  public Foo(String s) {
    throw new RuntimeException("stub!");
  }

  public String getName() {
    throw new RuntimeException("stub!");
  }

  public void findFooById(int i) {
    throw new RuntimeException("stub!");
  }
}
