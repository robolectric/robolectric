package org.robolectric.internal.bytecode.testing;

public class AnUninstrumentedClassWithToString {
  @Override
  public String toString() {
    return "baaaaaah";
  }
}
